package com.shaalevikas.app.ui

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaalevikas.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ShaaleViewModel(
    private val repository: FirebaseShaaleRepository = FirebaseShaaleRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(ShaaleUiState())
    val state: StateFlow<ShaaleUiState> = _state.asStateFlow()

    init {
        checkUserStatus()
        observeBackend()
        checkFirebaseConfig()
    }

    private fun checkFirebaseConfig() {
        if (!repository.isFirebaseAvailable) {
            _state.value = _state.value.copy(isFirebaseConfigured = false)
        }
    }

    private fun checkUserStatus() {
        val uid = repository.currentUid
        if (uid != null) {
            viewModelScope.launch {
                val profile = repository.getUserProfile(uid)
                _state.value = _state.value.copy(
                    isSignedIn = true,
                    userPhone = repository.currentUserPhone.orEmpty(),
                    userName = profile?.name.orEmpty(),
                    role = profile?.role ?: UserRole.Alumni,
                    userPhotoUrl = profile?.photoUrl
                )
            }
        }
    }

    fun selectRole(role: UserRole) {
        _state.value = _state.value.copy(role = role)
    }

    fun selectNeed(need: SchoolNeed) {
        _state.value = _state.value.copy(selectedNeed = need)
    }

    fun setCategory(category: NeedCategory) {
        _state.value = _state.value.copy(category = category)
    }

    fun setSearch(query: String) {
        _state.value = _state.value.copy(search = query)
    }

    fun signIn(email: String, pass: String) {
        _state.value = _state.value.copy(isBusy = true, message = null)
        viewModelScope.launch {
            runCatching { repository.signInWithEmail(email, pass) }
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isSignedIn = true,
                        userPhone = user.phone.ifBlank { email },
                        userName = user.name,
                        role = user.role,
                        userPhotoUrl = user.photoUrl,
                        isBusy = false,
                        message = "Signed in"
                    )
                }
                .onFailure { _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Sign in failed") }
        }
    }

    fun signUp(email: String, pass: String) {
        _state.value = _state.value.copy(isBusy = true, message = null)
        viewModelScope.launch {
            runCatching { repository.signUpWithEmail(email, pass, _state.value.role) }
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isSignedIn = true,
                        userPhone = user.phone.ifBlank { email },
                        userName = user.name,
                        role = user.role,
                        userPhotoUrl = user.photoUrl,
                        isBusy = false,
                        message = "Account created"
                    )
                }
                .onFailure { _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Sign up failed") }
        }
    }

    fun forgotPassword(email: String) {
        _state.value = _state.value.copy(message = "Password reset link sent to $email")
    }

    fun sendOtp(activity: Activity, phone: String) {
        _state.value = _state.value.copy(isBusy = true, message = null)
        repository.sendOtp(activity, phone) { result ->
            _state.value = _state.value.copy(
                isBusy = false,
                otpSent = result.isSuccess,
                message = result.exceptionOrNull()?.localizedMessage ?: "OTP sent"
            )
        }
    }

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching { repository.verifyOtp(code, _state.value.role) }
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isSignedIn = true,
                        userPhone = user.phone,
                        role = user.role,
                        userPhotoUrl = user.photoUrl,
                        isBusy = false,
                        message = "Signed in"
                    )
                }
                .onFailure { _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Sign in failed") }
        }
    }

    fun demoSignIn() {
        _state.value = _state.value.copy(
            isSignedIn = true, 
            userPhone = "+91 99999 88888", 
            userName = "Alumni User",
            role = UserRole.Alumni,
            message = "Demo session active"
        )
    }

    fun signOut() {
        repository.signOut()
        _state.value = ShaaleUiState()
    }

    fun suggestCost(title: String, description: String, category: NeedCategory, onResult: (Long, String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching { repository.suggestCost(title, description, category) }
                .onSuccess { (estimate, reason) ->
                    onResult(estimate, reason)
                    _state.value = _state.value.copy(isBusy = false, message = "Cost suggestion ready")
                }
                .onFailure { _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Could not suggest cost") }
        }
    }

    fun createNeed(context: android.content.Context, title: String, description: String, category: NeedCategory, cost: Long, reason: String, beforeImageUri: Uri?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            val currentState = _state.value
            
            runCatching {
                val beforeUrl = beforeImageUri?.let { uri ->
                    if (currentState.isFirebaseConfigured) {
                        repository.uploadImage(uri, "needs/before_${System.currentTimeMillis()}.jpg", context)
                    } else {
                        uri.toString() // Use local URI for demo
                    }
                }
                
                val need = SchoolNeed(
                    id = if (currentState.isFirebaseConfigured) "" else "demo-${System.currentTimeMillis()}",
                    schoolId = "school-${currentState.userName.hashCode()}",
                    schoolName = if (currentState.userName.isNotBlank()) "${currentState.userName}'s School" else "Lakshmi Public School",
                    title = title,
                    description = description,
                    category = category,
                    location = "Bengaluru, Karnataka",
                    headmasterName = currentState.userName.ifBlank { "Principal" },
                    estimatedCost = cost,
                    costReason = reason,
                    beforePhotoUrl = beforeUrl,
                    status = NeedStatus.Open
                )
                
                if (currentState.isFirebaseConfigured) {
                    repository.createNeed(need)
                }
                
                // Optimistically update the local list immediately in both modes
                val updatedList = listOf(need) + currentState.needs
                _state.value = _state.value.copy(needs = updatedList, isBusy = false, message = "Need published successfully")
            }.onFailure {
                _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Failed to publish need")
            }
        }
    }

    fun completeNeed(context: android.content.Context, needId: String, update: String, afterImageUri: Uri?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching {
                val afterUrl = afterImageUri?.let { uri ->
                    repository.uploadImage(uri, "needs/after_${needId}_${System.currentTimeMillis()}.jpg", context)
                }
                repository.updateNeedStatus(needId, NeedStatus.Completed, update, afterUrl)
            }.onSuccess {
                _state.value = _state.value.copy(isBusy = false, message = "Need marked as completed!")
            }.onFailure {
                _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Failed to update status")
            }
        }
    }

    fun updateNeedImages(context: android.content.Context, needId: String, beforeImageUri: Uri?, afterImageUri: Uri?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching {
                val beforeUrl = beforeImageUri?.let { uri ->
                    repository.uploadImage(uri, "needs/before_${System.currentTimeMillis()}.jpg", context)
                }
                val afterUrl = afterImageUri?.let { uri ->
                    repository.uploadImage(uri, "needs/after_${System.currentTimeMillis()}.jpg", context)
                }
                repository.updateNeedImages(needId, beforeUrl, afterUrl)
            }.onSuccess {
                _state.value = _state.value.copy(isBusy = false, message = "Images updated successfully")
            }.onFailure {
                _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Failed to update images")
            }
        }
    }

    fun pledge(amount: Long, note: String) {
        val need = _state.value.selectedNeed ?: return
        val name = _state.value.userName.ifBlank { "Alumni" }
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching { repository.pledge(need, amount, note, name) }
                .onSuccess { 
                    _state.value = _state.value.copy(isBusy = false, message = "Pledge of ₹$amount successful!") 
                }
                .onFailure { 
                    _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Pledge failed") 
                }
        }
    }

    fun updateProfileImage(context: android.content.Context, uri: Uri) {
        // Optimistically update UI
        _state.value = _state.value.copy(userPhotoUrl = uri.toString())
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching { repository.updateProfileImage(context, uri) }
                .onSuccess { 
                    _state.value = _state.value.copy(isBusy = false, message = "Profile image updated!") 
                }
                .onFailure { 
                    if (_state.value.isFirebaseConfigured) {
                        _state.value = _state.value.copy(isBusy = false, message = it.localizedMessage ?: "Failed to update profile image")
                    } else {
                        _state.value = _state.value.copy(isBusy = false)
                    }
                }
        }
    }

    fun generateAiSummary(title: String, category: NeedCategory, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            val summary = repository.generateAiSummary(title, category)
            onResult(summary)
            _state.value = _state.value.copy(isBusy = false)
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(userName = name)
        viewModelScope.launch {
            runCatching { repository.updateUserName(name) }
        }
    }

    private fun observeBackend() {
        viewModelScope.launch {
            repository.observeNeeds()
                .catch { emit(demoNeeds) }
                .collect { list -> 
                    _state.value = _state.value.copy(needs = list)
                    _state.value.selectedNeed?.let { selected ->
                        list.find { it.id == selected.id }?.let { fresh ->
                            _state.value = _state.value.copy(selectedNeed = fresh)
                        }
                    }
                }
        }
        viewModelScope.launch {
            repository.observeLeaderboard()
                .catch { emit(demoLeaderboard) }
                .collect { entries -> _state.value = _state.value.copy(leaderboard = entries) }
        }
        viewModelScope.launch {
            repository.observeRecentPledges()
                .collect { list -> _state.value = _state.value.copy(recentPledges = list) }
        }
        
        // Observe user-specific pledges whenever signed in
        viewModelScope.launch {
            // Wait for sign in
            while (repository.currentUid == null) {
                kotlinx.coroutines.delay(1000)
            }
            val uid = repository.currentUid!!
            repository.observeUserPledges(uid)
                .collect { list -> _state.value = _state.value.copy(userPledges = list) }
        }
    }
}

data class ShaaleUiState(
    val role: UserRole = UserRole.Alumni,
    val isSignedIn: Boolean = false,
    val userPhone: String = "",
    val userName: String = "",
    val otpSent: Boolean = false,
    val isBusy: Boolean = false,
    val isFirebaseConfigured: Boolean = true,
    val search: String = "",
    val category: NeedCategory = NeedCategory.All,
    val needs: List<SchoolNeed> = demoNeeds,
    val recentPledges: List<Pledge> = emptyList(),
    val userPledges: List<Pledge> = emptyList(),
    val leaderboard: List<LeaderboardEntry> = demoLeaderboard,
    val selectedNeed: SchoolNeed? = demoNeeds.firstOrNull(),
    val message: String? = null,
    val userPhotoUrl: String? = null
) {
    val filteredNeeds: List<SchoolNeed>
        get() = needs.filter { need ->
            (category == NeedCategory.All || need.category == category) &&
                (search.isBlank() || need.title.contains(search, true) || need.schoolName.contains(search, true))
        }
}
