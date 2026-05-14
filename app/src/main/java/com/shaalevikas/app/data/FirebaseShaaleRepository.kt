package com.shaalevikas.app.data

import android.app.Activity
import android.net.Uri
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebaseShaaleRepository {
    private val auth: FirebaseAuth? by lazy { runCatching { FirebaseAuth.getInstance() }.getOrNull() }
    private val firestore: FirebaseFirestore? by lazy { runCatching { FirebaseFirestore.getInstance() }.getOrNull() }
    private val storage: FirebaseStorage? by lazy { runCatching { FirebaseStorage.getInstance() }.getOrNull() }
    private val functions: FirebaseFunctions? by lazy { runCatching { FirebaseFunctions.getInstance() }.getOrNull() }
    private var verificationId: String? = null

    val currentUid: String? get() = auth?.currentUser?.uid
    val currentUserPhone: String? get() = auth?.currentUser?.phoneNumber
    val isFirebaseAvailable: Boolean get() = auth != null && firestore != null

    fun sendOtp(activity: Activity, rawPhone: String, onResult: (Result<Unit>) -> Unit) {
        val firebaseAuth = auth ?: return onResult(Result.failure(IllegalStateException("Add app/google-services.json to enable Firebase Phone Auth")))
        val phone = if (rawPhone.startsWith("+")) rawPhone else "+91$rawPhone"
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    val error = task.exception ?: IllegalStateException("OTP verification failed")
                    onResult(if (task.isSuccessful) Result.success(Unit) else Result.failure(error))
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                onResult(Result.failure(exception))
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                onResult(Result.success(Unit))
            }
        }
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyOtp(code: String, role: UserRole): AppUser {
        val firebaseAuth = auth ?: error("Add app/google-services.json to enable Firebase Phone Auth")
        val id = verificationId ?: error("Request OTP before verifying")
        val credential = PhoneAuthProvider.getCredential(id, code)
        firebaseAuth.signInWithCredential(credential).await()
        return ensureUser(role)
    }

    suspend fun signInWithEmail(email: String, pass: String): AppUser {
        val firebaseAuth = auth ?: error("Add app/google-services.json to enable Firebase Auth")
        firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        return ensureUser(UserRole.Alumni) // Default role, will be updated from profile
    }

    suspend fun signUpWithEmail(email: String, pass: String, role: UserRole): AppUser {
        val firebaseAuth = auth ?: error("Add app/google-services.json to enable Firebase Auth")
        firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        return ensureUser(role)
    }

    suspend fun getUserProfile(uid: String): AppUser? {
        val database = firestore ?: return null
        return try {
            val doc = database.collection("users").document(uid).get().await()
            if (doc.exists()) {
                AppUser(
                    uid = doc.id,
                    name = doc.getString("name").orEmpty(),
                    phone = doc.getString("phone").orEmpty(),
                    role = UserRole.valueOf(doc.getString("role") ?: UserRole.Alumni.name),
                    schoolId = doc.getString("schoolId"),
                    schoolName = doc.getString("schoolName"),
                    city = doc.getString("city"),
                    photoUrl = doc.getString("photoUrl")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun ensureUser(role: UserRole): AppUser {
        val user = auth?.currentUser ?: error("User is not signed in")
        val database = firestore ?: error("Add app/google-services.json to enable Firestore")
        val existing = getUserProfile(user.uid)
        if (existing != null) return existing

        val profile = AppUser(uid = user.uid, phone = user.phoneNumber.orEmpty(), role = role)
        database.collection("users").document(user.uid).set(profile, SetOptions.merge()).await()
        return profile
    }

    fun signOut() {
        auth?.signOut()
    }

    suspend fun uploadImage(uri: Uri, path: String, context: android.content.Context): String {
        val ref = storage?.reference?.child(path) ?: error("Firebase Storage not available")
        
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: error("Could not open image")
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) error("Invalid image file")

            val compressedFile = java.io.File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            val out = java.io.FileOutputStream(compressedFile)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
            out.flush()
            out.close()
            
            ref.putFile(Uri.fromFile(compressedFile)).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }

    fun observeNeeds(): Flow<List<SchoolNeed>> = callbackFlow {
        val database = firestore
        if (database == null) {
            trySend(demoNeeds)
            close()
            return@callbackFlow
        }
        val registration = database.collection("needs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(demoNeeds)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc -> doc.toNeed() }
                trySend(items.ifEmpty { demoNeeds })
            }
        awaitClose { registration.remove() }
    }

    fun observeRecentPledges(): Flow<List<Pledge>> = callbackFlow {
        val database = firestore
        if (database == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = database.collection("pledges")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.documents.orEmpty().map { doc ->
                    Pledge(
                        id = doc.id,
                        needId = doc.getString("needId").orEmpty(),
                        schoolName = doc.getString("schoolName").orEmpty(),
                        alumniId = doc.getString("alumniId").orEmpty(),
                        alumniName = doc.getString("alumniName").orEmpty(),
                        amount = doc.getLong("amount") ?: 0L,
                        note = doc.getString("note").orEmpty(),
                        createdAt = doc.getTimestamp("createdAt")
                    )
                }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeUserPledges(uid: String): Flow<List<Pledge>> = callbackFlow {
        val database = firestore
        if (database == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = database.collection("pledges")
            .whereEqualTo("alumniId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.documents.orEmpty().map { doc ->
                    Pledge(
                        id = doc.id,
                        needId = doc.getString("needId").orEmpty(),
                        schoolName = doc.getString("schoolName").orEmpty(),
                        alumniId = doc.getString("alumniId").orEmpty(),
                        alumniName = doc.getString("alumniName").orEmpty(),
                        amount = doc.getLong("amount") ?: 0L,
                        note = doc.getString("note").orEmpty(),
                        createdAt = doc.getTimestamp("createdAt")
                    )
                }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val database = firestore
        if (database == null) {
            trySend(demoLeaderboard)
            close()
            return@callbackFlow
        }
        val registration = database.collection("leaderboard")
            .orderBy("totalPledged", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(demoLeaderboard)
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents.orEmpty().map { doc ->
                    LeaderboardEntry(
                        userId = doc.id,
                        name = doc.getString("name").orEmpty().ifBlank { "Alumni" },
                        totalPledged = doc.getLong("totalPledged") ?: 0L
                    )
                }
                trySend(entries.ifEmpty { demoLeaderboard })
            }
        awaitClose { registration.remove() }
    }

    suspend fun createNeed(need: SchoolNeed): String {
        val database = firestore ?: error("Add app/google-services.json to enable Firestore")
        val doc = database.collection("needs").document()
        doc.set(
            mapOf(
                "schoolId" to need.schoolId,
                "schoolName" to need.schoolName,
                "title" to need.title,
                "description" to need.description,
                "category" to need.category.name,
                "location" to need.location,
                "headmasterName" to need.headmasterName,
                "estimatedCost" to need.estimatedCost,
                "collectedAmount" to 0L,
                "pledgeCount" to 0L,
                "costReason" to need.costReason,
                "beforePhotoUrl" to need.beforePhotoUrl,
                "status" to NeedStatus.Open.name,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return doc.id
    }

    suspend fun updateNeedStatus(needId: String, status: NeedStatus, completionUpdate: String?, afterPhotoUrl: String?) {
        val database = firestore ?: error("Add app/google-services.json to enable Firestore")
        val updates = mutableMapOf<String, Any>(
            "status" to status.name
        )
        if (completionUpdate != null) updates["completionUpdate"] = completionUpdate
        if (afterPhotoUrl != null) updates["afterPhotoUrl"] = afterPhotoUrl
        if (status == NeedStatus.Completed) updates["completedAt"] = FieldValue.serverTimestamp()
        
        database.collection("needs").document(needId).update(updates).await()
    }

    suspend fun updateNeedImages(needId: String, beforePhotoUrl: String?, afterPhotoUrl: String?) {
        val database = firestore ?: error("Add app/google-services.json to enable Firestore")
        val updates = mutableMapOf<String, Any>()
        if (beforePhotoUrl != null) updates["beforePhotoUrl"] = beforePhotoUrl
        if (afterPhotoUrl != null) updates["afterPhotoUrl"] = afterPhotoUrl
        
        if (updates.isNotEmpty()) {
            database.collection("needs").document(needId).update(updates).await()
        }
    }

    suspend fun updateProfileImage(context: android.content.Context, uri: Uri) {
        val uid = currentUid ?: return
        val url = uploadImage(uri, "profiles/$uid.jpg", context)
        firestore?.collection("users")?.document(uid)?.update("photoUrl", url)?.await()
    }

    suspend fun updateUserName(name: String) {
        val uid = currentUid ?: return
        firestore?.collection("users")?.document(uid)?.update("name", name)?.await()
    }

    suspend fun suggestCost(title: String, description: String, category: NeedCategory): Pair<Long, String> {
        val client = functions ?: return localCostSuggestion(title, description, category)
        val result = client.getHttpsCallable("suggestCost")
            .call(mapOf("title" to title, "description" to description, "category" to category.name))
            .await()
        val data = result.getData() as? Map<*, *> ?: return localCostSuggestion(title, description, category)
        val estimate = (data["estimate"] as? Number)?.toLong() ?: localCostSuggestion(title, description, category).first
        val reason = data["reason"] as? String ?: localCostSuggestion(title, description, category).second
        return estimate to reason
    }

    suspend fun generateAiSummary(title: String, category: NeedCategory): String {
        // In a real Blaze-plan app, this calls Vertex AI or a Cloud Function.
        // For Spark-plan, we use a smart local template generator.
        delay(800) // Simulate AI processing
        return when(category) {
            NeedCategory.Roof -> "The school's roof over the primary wing is severely compromised. Rainwater leaks are damaging books and equipment, creating an unsafe learning environment for 50+ students."
            NeedCategory.Sanitation -> "Current sanitation facilities are inadequate and require immediate renovation to ensure hygiene and safety for girl students, reducing drop-out rates."
            NeedCategory.Computers -> "Modern education requires digital literacy. We aim to establish a basic lab with 5 workstations to help students compete in the digital age."
            else -> "A critical infrastructure gap in $title requires community support. Every contribution directly impacts the daily learning experience of our rural students."
        }
    }

    private suspend fun delay(ms: Long) = kotlinx.coroutines.delay(ms)

    suspend fun pledge(need: SchoolNeed, amount: Long, note: String, alumniName: String) {
        val database = firestore ?: error("Add app/google-services.json to enable Firestore")
        val uid = currentUid ?: error("Sign in before pledging")
        
        val batch = database.batch()
        
        // 1. Create the pledge record
        val pledgeRef = database.collection("pledges").document()
        batch.set(pledgeRef, mapOf(
            "needId" to need.id,
            "schoolName" to need.schoolName,
            "alumniId" to uid,
            "alumniName" to alumniName,
            "amount" to amount,
            "note" to note,
            "createdAt" to FieldValue.serverTimestamp()
        ))
        
        // 2. Update the need total and count (Atomic increment)
        val needRef = database.collection("needs").document(need.id)
        batch.update(needRef, mapOf(
            "collectedAmount" to FieldValue.increment(amount),
            "pledgeCount" to FieldValue.increment(1)
        ))

        // 3. Update the leaderboard (Atomic increment)
        val leaderboardRef = database.collection("leaderboard").document(uid)
        batch.set(leaderboardRef, mapOf(
            "name" to alumniName,
            "totalPledged" to FieldValue.increment(amount)
        ), SetOptions.merge())
        
        batch.commit().await()
    }
}

private fun localCostSuggestion(title: String, description: String, category: NeedCategory): Pair<Long, String> {
    val base = when (category) {
        NeedCategory.Roof -> 45000L
        NeedCategory.LeakingRoof -> 15000L
        NeedCategory.Furniture -> 32000L
        NeedCategory.BrokenDesk -> 8000L
        NeedCategory.Library -> 25000L
        NeedCategory.Computers -> 150000L
        NeedCategory.Sanitation -> 60000L
        NeedCategory.Painting -> 20000L
        NeedCategory.All -> 30000L
    }
    val detailFactor = (description.length.coerceAtMost(200) / 200.0 * 0.25)
    val estimate = ((base * (1 + detailFactor)).toLong() / 500) * 500
    return estimate to "Estimated for $title: materials, transport, labor and local vendor variation."
}

private fun com.google.firebase.firestore.DocumentSnapshot.toNeed(): SchoolNeed {
    val category = runCatching { NeedCategory.valueOf(getString("category") ?: NeedCategory.Roof.name) }.getOrDefault(NeedCategory.Roof)
    val status = runCatching { NeedStatus.valueOf(getString("status") ?: NeedStatus.Open.name) }.getOrDefault(NeedStatus.Open)
    return SchoolNeed(
        id = id,
        schoolId = getString("schoolId").orEmpty(),
        schoolName = getString("schoolName").orEmpty(),
        title = getString("title").orEmpty(),
        description = getString("description").orEmpty(),
        category = category,
        location = getString("location").orEmpty(),
        headmasterName = getString("headmasterName").orEmpty(),
        estimatedCost = getLong("estimatedCost") ?: 0L,
        collectedAmount = getLong("collectedAmount") ?: 0L,
        pledgeCount = getLong("pledgeCount") ?: 0L,
        costReason = getString("costReason").orEmpty(),
        beforePhotoUrl = getString("beforePhotoUrl"),
        afterPhotoUrl = getString("afterPhotoUrl"),
        status = status,
        createdAt = getTimestamp("createdAt") ?: Timestamp.now(),
        completedAt = getTimestamp("completedAt"),
        completionUpdate = getString("completionUpdate")
    )
}
