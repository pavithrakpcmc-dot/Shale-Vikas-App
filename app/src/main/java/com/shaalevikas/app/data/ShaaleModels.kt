package com.shaalevikas.app.data

import com.google.firebase.Timestamp

enum class UserRole { Alumni, Headmaster }
enum class NeedCategory(val label: String) { 
    All("All"), 
    Roof("Roof"), 
    LeakingRoof("Leaking Roof"),
    Furniture("Furniture"), 
    BrokenDesk("Broken Desk"),
    Library("Library"), 
    Computers("Computers"), 
    Sanitation("Sanitation"),
    Painting("Painting")
}
enum class NeedStatus(val label: String) { Open("Open"), InProgress("In Progress"), Completed("Completed") }

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.Alumni,
    val schoolId: String? = null,
    val schoolName: String? = null,
    val city: String? = null,
    val photoUrl: String? = null
)

data class SchoolNeed(
    val id: String = "",
    val schoolId: String = "",
    val schoolName: String = "",
    val title: String = "",
    val description: String = "",
    val category: NeedCategory = NeedCategory.Roof,
    val location: String = "",
    val headmasterName: String = "",
    val estimatedCost: Long = 0,
    val collectedAmount: Long = 0,
    val pledgeCount: Long = 0,
    val costReason: String = "",
    val beforePhotoUrl: String? = null,
    val afterPhotoUrl: String? = null,
    val status: NeedStatus = NeedStatus.Open,
    val createdAt: Timestamp? = null,
    val completedAt: Timestamp? = null,
    val completionUpdate: String? = null
) {
    val progress: Float get() = if (estimatedCost <= 0) 0f else collectedAmount.toFloat() / estimatedCost.toFloat()
}

data class Pledge(
    val id: String = "",
    val needId: String = "",
    val schoolName: String = "",
    val alumniId: String = "",
    val alumniName: String = "",
    val amount: Long = 0,
    val note: String = "",
    val createdAt: Timestamp? = null
)

data class SchoolUpdate(
    val id: String = "",
    val needId: String = "",
    val title: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val createdAt: Timestamp? = null
)

data class LeaderboardEntry(
    val userId: String = "",
    val name: String = "",
    val totalPledged: Long = 0
)

val demoNeeds = listOf(
    SchoolNeed(
        id = "demo-roof",
        schoolId = "lakshmi-public-school",
        schoolName = "Lakshmi Public School",
        title = "Roof Repair - Block A",
        description = "Urgent repair for classroom roof before monsoon.",
        category = NeedCategory.Roof,
        location = "Bengaluru, Karnataka",
        headmasterName = "Ramesh Kumar",
        estimatedCost = 45000,
        collectedAmount = 28500,
        pledgeCount = 4,
        costReason = "5 sheets corrugated metal @ Rs. 4,500/sheet, labor and installation",
        status = NeedStatus.InProgress
    ),
    SchoolNeed(
        id = "demo-furniture",
        schoolId = "krishna-public-school",
        schoolName = "Krishna Public School",
        title = "Classroom Furniture",
        description = "Benches and desks for the higher primary classroom.",
        category = NeedCategory.Furniture,
        location = "Mysuru, Karnataka",
        headmasterName = "Priya Narayan",
        estimatedCost = 32000,
        collectedAmount = 14400,
        pledgeCount = 3,
        costReason = "20 desks with benches, local transport and fitting",
        status = NeedStatus.Open
    )
)

val demoLeaderboard = listOf(
    LeaderboardEntry("demo-1", "Rajesh Kumar", 85000),
    LeaderboardEntry("demo-2", "Priya Sharma", 62500),
    LeaderboardEntry("demo-3", "You", 15000),
    LeaderboardEntry("demo-4", "Amit Patel", 45200)
)
