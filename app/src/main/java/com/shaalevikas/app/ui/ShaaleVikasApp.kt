package com.shaalevikas.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.shaalevikas.app.R
import com.shaalevikas.app.data.*

enum class AppScreen { Role, Login, Home, Detail, CreateNeed, Fame, Pledges, Reviews, Profile }

private val BrandBlue = Color(0xFF1E3A8A)
private val BrandAccent = Color(0xFF3B82F6)
private val BrandGradient = Brush.verticalGradient(listOf(BrandBlue, BrandAccent))
private val Paper = Color(0xFFF8FAFC)
private val Muted = Color(0xFF64748B)
private val Line = Color(0xFFE2E8F0)
private val Success = Color(0xFF10B981)
private val SuccessAccent = Color(0xFF34D399)
private val SuccessGradient = Brush.horizontalGradient(listOf(Success, SuccessAccent))

@Composable
fun ShaaleVikasApp(viewModel: ShaaleViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var screen by rememberSaveable { mutableStateOf(AppScreen.Role) }

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            if (screen == AppScreen.Login || screen == AppScreen.Role) {
                screen = AppScreen.Home
            }
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BrandBlue,
            secondary = BrandAccent,
            surface = Color.White,
            background = Paper
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Paper) {
            Crossfade(targetState = screen, label = "screen") { current ->
                when (current) {
                    AppScreen.Role -> FrontPage(state.role, viewModel::selectRole) { screen = AppScreen.Login }
                    AppScreen.Login -> LoginScreen(
                        state = state,
                        onBack = { screen = AppScreen.Role },
                        onSignIn = viewModel::signIn,
                        onSignUp = viewModel::signUp,
                        onForgot = viewModel::forgotPassword,
                        onDemo = { viewModel.demoSignIn() }
                    )
                    AppScreen.Home -> HomeScreen(
                        state = state,
                        onOpenNeed = { viewModel.selectNeed(it); screen = AppScreen.Detail },
                        onCreateNeed = { screen = AppScreen.CreateNeed },
                        onSearch = viewModel::setSearch,
                        onCategory = viewModel::setCategory,
                        onTab = { screen = it }
                    )
                    AppScreen.Detail -> NeedDetailScreen(
                        state.selectedNeed, 
                        state.role, 
                        state.recentPledges.filter { it.needId == state.selectedNeed?.id },
                        onPledge = { amount, note -> viewModel.pledge(amount, note) },
                        onComplete = { update, uri -> viewModel.completeNeed(state.selectedNeed?.id ?: "", update, uri) },
                        onUpdateImages = { beforeUri, afterUri -> viewModel.updateNeedImages(state.selectedNeed?.id ?: "", beforeUri, afterUri) }
                    ) { screen = AppScreen.Home }
                    
                    AppScreen.CreateNeed -> CreateNeedScreen(
                        onBack = { screen = AppScreen.Home },
                        onSuggest = viewModel::suggestCost,
                        onCreated = { title, desc, category, cost, reason, uri ->
                            viewModel.createNeed(title, desc, category, cost, reason, uri)
                            screen = AppScreen.Home
                        }
                    )
                    AppScreen.Fame -> FameScreen(state.leaderboard) { screen = it }
                    AppScreen.Pledges -> MyPledgesScreen(state.userPledges) { screen = it }
                    AppScreen.Reviews -> SchoolUpdatesScreen(state.needs) { screen = it }
                    AppScreen.Profile -> ProfileScreen(
                        phone = state.userPhone,
                        userName = state.userName,
                        role = state.role,
                        photoUrl = state.userPhotoUrl,
                        onUpdateName = { viewModel.updateName(it) },
                        onUpdateImage = { uri -> viewModel.updateProfileImage(uri) },
                        onSignOut = { viewModel.signOut(); screen = AppScreen.Role },
                        onTab = { screen = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun FrontPage(role: UserRole, onRole: (UserRole) -> Unit, onContinue: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = BrandBlue
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Shaale-Vikas",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandBlue,
                letterSpacing = (-1).sp
            )
            Text(
                "Restoring Education, One School at a Time",
                textAlign = TextAlign.Center,
                color = Muted,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(48.dp))
            
            Text("How would you like to contribute?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = BrandBlue)
            Spacer(Modifier.height(16.dp))
            
            RoleCard(
                "I am an Alumni", 
                "Empower your alma mater with targeted support.", 
                role == UserRole.Alumni,
                Icons.Default.Person
            ) { onRole(UserRole.Alumni) }
            
            Spacer(Modifier.height(12.dp))
            
            RoleCard(
                "I am a Headmaster", 
                "Voice your school's needs and showcase progress.", 
                role == UserRole.Headmaster,
                Icons.Default.AdminPanelSettings
            ) { onRole(UserRole.Headmaster) }
            
            Spacer(Modifier.height(40.dp))
            PrimaryButton("Get Started", onClick = onContinue)
        }
    }
}

@Composable
private fun RoleCard(title: String, body: String, selected: Boolean, icon: ImageVector, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (selected) BrandBlue else Color.Transparent),
        colors = CardDefaults.cardColors(if (selected) Color.White else Color.White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(if (selected) 8.dp else 2.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(if (selected) BrandBlue else Paper), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (selected) Color.White else BrandBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = if (selected) BrandBlue else Color.Black)
                Text(body, color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun LoginScreen(state: ShaaleUiState, onBack: () -> Unit, onSignIn: (String, String) -> Unit, onSignUp: (String, String) -> Unit, onForgot: (String) -> Unit, onDemo: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var isSignUp by rememberSaveable { mutableStateOf(false) }
    
    AppScaffold(title = if (isSignUp) "Create Account" else "Sign In", showBottomBar = false, onBack = onBack) {
        Box(Modifier.fillMaxSize()) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.2f
            )
            Column(Modifier.padding(28.dp).verticalScroll(rememberScrollState())) {
            Text(if (isSignUp) "Join the Change" else "Welcome back!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(if (isSignUp) "Sign up as ${state.role.name} to start contributing." else "Sign in to continue as ${state.role.name}", color = Muted)
            Spacer(Modifier.height(32.dp))
            
            FieldLabel("Email Address")
            Input(email, { email = it }, "email@example.com", KeyboardType.Email, leadingIcon = Icons.Default.Email)
            
            FieldLabel("Password")
            Input(
                value = password, 
                onChange = { password = it }, 
                placeholder = "••••••••", 
                keyboardType = KeyboardType.Password,
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Muted)
                    }
                }
            )
            
            if (!isSignUp) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { if (email.isNotBlank()) onForgot(email) }) {
                        Text("Forgot Password?", color = BrandBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            PrimaryButton(if (isSignUp) "Create Account" else "Sign In") {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (isSignUp) onSignUp(email, password) else onSignIn(email, password)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isSignUp) "Already have an account?" else "New here?", color = Muted, fontSize = 14.sp)
                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(if (isSignUp) "Sign In" else "Create Account", color = BrandBlue, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDemo, modifier = Modifier.fillMaxWidth()) {
                Text("Try Demo Account", color = BrandAccent, fontWeight = FontWeight.SemiBold)
            }
            
            if (!state.isFirebaseConfigured) {
                Card(
                    Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800))
                        Spacer(Modifier.width(12.dp))
                        Text("Firebase not configured. App is running in Demo Mode (add google-services.json to enable full features).", color = Color(0xFFE65100), fontSize = 12.sp)
                    }
                }
            }

            state.message?.let { 
                Text(it, color = if (it.contains("fail", true) || it.contains("Error", true)) Color.Red else Success, 
                     fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), textAlign = TextAlign.Center) 
            }
        }
    }
}
}

@Composable
private fun HomeScreen(state: ShaaleUiState, onOpenNeed: (SchoolNeed) -> Unit, onCreateNeed: () -> Unit, onSearch: (String) -> Unit, onCategory: (NeedCategory) -> Unit, onTab: (AppScreen) -> Unit) {
    AppScaffold(title = "Dashboard", selected = AppScreen.Home, onTab = onTab) {
        Box(Modifier.fillMaxSize()) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.05f
            )
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { DashboardHeader(state.role, state.userName, state.needs.size, state.needs.sumOf { it.collectedAmount }) }
                
                item {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Text("Current Needs", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.search,
                            onValueChange = onSearch,
                            placeholder = { Text("Search by school or need...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NeedCategory.entries.forEach { FilterChip(it.label, state.category == it) { onCategory(it) } }
                        }
                    }
                }

                if (state.role == UserRole.Headmaster) {
                    item {
                        Button(
                            onClick = onCreateNeed,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Create New Need", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(state.filteredNeeds.filter { it.status != NeedStatus.Completed }) { NeedCard(it) { onOpenNeed(it) } }
                
                if (state.filteredNeeds.none { it.status != NeedStatus.Completed }) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No active needs found.", color = Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(role: UserRole, userName: String, activeCount: Int, totalImpact: Long) {
    Column(
        Modifier.fillMaxWidth().background(BrandGradient).padding(24.dp)
    ) {
        Text("Good Morning,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Text(if (userName.isNotBlank()) userName else if (role == UserRole.Headmaster) "Principal" else "Welcome, Alumni", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Active Needs", activeCount.toString(), Icons.AutoMirrored.Filled.Assignment, Modifier.weight(1f))
            StatCard("Impact Made", currency(totalImpact), Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.15f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun NeedCard(need: SchoolNeed, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Paper), contentAlignment = Alignment.Center) {
                    if (need.beforePhotoUrl != null) {
                        AsyncImage(model = need.beforePhotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(
                            when(need.category) {
                                NeedCategory.Roof -> Icons.Default.Home
                                NeedCategory.LeakingRoof -> Icons.Default.WaterDamage
                                NeedCategory.Furniture -> Icons.Default.Chair
                                NeedCategory.BrokenDesk -> Icons.Default.EventSeat
                                NeedCategory.Library -> Icons.AutoMirrored.Filled.MenuBook
                                NeedCategory.Computers -> Icons.Default.Computer
                                NeedCategory.Sanitation -> Icons.Default.CleanHands
                                NeedCategory.Painting -> Icons.Default.FormatPaint
                                else -> Icons.Default.Business
                            }, 
                            null, tint = BrandBlue
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(need.schoolName, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(need.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(currency(need.estimatedCost), fontWeight = FontWeight.SemiBold, color = BrandBlue)
                Text("${(need.progress * 100).toInt()}% funded", fontWeight = FontWeight.Medium, color = if(need.progress >= 1f) Success else BrandAccent)
            }
            Spacer(Modifier.height(8.dp))
            ProgressBar(need.progress)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, null, tint = Muted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${need.pledgeCount} alumni pledged", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun NeedDetailScreen(need: SchoolNeed?, role: UserRole, supporters: List<Pledge>, onPledge: (Long, String) -> Unit, onComplete: (String, Uri?) -> Unit, onUpdateImages: (Uri?, Uri?) -> Unit, onBack: () -> Unit) {
    if (need == null) return
    var showPledgeDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showUpdatePhotosDialog by remember { mutableStateOf(false) }

    AppScaffold(title = "Need Details", showBottomBar = false, onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            Box(Modifier.fillMaxWidth().height(220.dp).background(BrandBlue)) {
                if (need.beforePhotoUrl != null) {
                    AsyncImage(model = need.beforePhotoUrl, contentDescription = "Before Photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    PhotoBox("No Photo Available")
                }
                Box(Modifier.align(Alignment.BottomStart).padding(20.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("BEFORE STATUS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(need.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text(need.schoolName, color = BrandAccent, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusChip(need.status)
                        Spacer(Modifier.height(8.dp))
                        IconButton(
                            onClick = { /* Share logic would go here, e.g., using Intent.ACTION_SEND */ },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Paper)
                        ) {
                            Icon(Icons.Default.Share, null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailChip(Icons.Default.LocationOn, need.location)
                    DetailChip(Icons.Default.Person, need.headmasterName)
                    DetailChip(Icons.Default.Category, need.category.label)
                }
                
                Spacer(Modifier.height(24.dp))
                SectionTitle("Funding Progress")
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Target", color = Muted, fontSize = 12.sp); Text(currency(need.estimatedCost), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                            Column(horizontalAlignment = Alignment.End) { Text("Raised", color = Muted, fontSize = 12.sp); Text(currency(need.collectedAmount), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Success) }
                        }
                        Spacer(Modifier.height(16.dp))
                        ProgressBar(need.progress, height = 8.dp)
                        Spacer(Modifier.height(8.dp))
                        Text("${(need.progress * 100).toInt()}% funded by ${need.pledgeCount} alumni", fontSize = 12.sp, color = Muted)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                SectionTitle("Problem Description")
                Text(need.description, lineHeight = 22.sp, color = Color.DarkGray)
                
                Spacer(Modifier.height(24.dp))
                SectionTitle("Cost Justification")
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, Line, RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Text(need.costReason.ifBlank { "Cost estimated based on local vendor standards." }, color = Muted)
                }

                if (need.status == NeedStatus.Completed || need.afterPhotoUrl != null) {
                    Spacer(Modifier.height(24.dp))
                    SectionTitle("IMPACT COMPARISON")
                    Row(Modifier.fillMaxWidth().height(200.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Line)) {
                            if (need.beforePhotoUrl != null) {
                                AsyncImage(model = need.beforePhotoUrl, contentDescription = "Before", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                            Box(Modifier.align(Alignment.BottomStart).padding(8.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("BEFORE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Line)) {
                            if (need.afterPhotoUrl != null) {
                                AsyncImage(model = need.afterPhotoUrl, contentDescription = "After", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                PhotoBox("After Pending")
                            }
                            Box(Modifier.align(Alignment.BottomStart).padding(8.dp).background(Success.copy(alpha = 0.8f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("AFTER", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(need.completionUpdate ?: "Work in progress. Photos will be updated regularly.", color = BrandBlue, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                
                if (supporters.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    SectionTitle("SUPPORTERS WALL")
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), border = BorderStroke(1.dp, Line)) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            supporters.take(3).forEach { supporter ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(32.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(supporter.alumniName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (supporter.note.isNotBlank()) {
                                            Text("\"${supporter.note}\"", color = Muted, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text(currency(supporter.amount), fontWeight = FontWeight.Bold, color = Success, fontSize = 13.sp)
                                }
                                if (supporter != supporters.take(3).last()) HorizontalDivider(color = Line.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                if (role == UserRole.Alumni && need.status != NeedStatus.Completed) {
                    PrimaryButton("Pledge Support") { showPledgeDialog = true }
                } else if (role == UserRole.Headmaster) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (need.status != NeedStatus.Completed) {
                            PrimaryButton("Mark as Completed", color = Success) { showCompleteDialog = true }
                        }
                        OutlinedButton(
                            onClick = { showUpdatePhotosDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandBlue)
                        ) {
                            Icon(Icons.Default.AddAPhoto, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Update Progress Photos", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showPledgeDialog) {
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPledgeDialog = false },
            title = { Text("Pledge Support") },
            text = {
                Column {
                    Text("Enter amount you'd like to pledge for this school need.", color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(500, 1000, 5000).forEach { amt ->
                            OutlinedButton(
                                onClick = { amount = amt.toString() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (amount == amt.toString()) BrandBlue else Line),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (amount == amt.toString()) BrandBlue else Muted)
                            ) {
                                Text("₹$amt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    Input(amount, { amount = it }, "Other amount", KeyboardType.Number, leadingIcon = Icons.Default.Payments)
                    Spacer(Modifier.height(12.dp))
                    FieldLabel("Add a Note (Optional)")
                    Input(note, { note = it }, "e.g., Hope this helps the children!", leadingIcon = Icons.Default.ChatBubbleOutline)
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    val value = amount.toLongOrNull() ?: 0L
                    if (value > 0) { onPledge(value, note); showPledgeDialog = false }
                }) { Text("Confirm Pledge", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showPledgeDialog = false }) { Text("Cancel") } }
        )
    }


    if (showUpdatePhotosDialog) {
        var beforeUri by remember { mutableStateOf<Uri?>(null) }
        var afterUri by remember { mutableStateOf<Uri?>(null) }
        val beforePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { beforeUri = it }
        val afterPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { afterUri = it }

        AlertDialog(
            onDismissRequest = { showUpdatePhotosDialog = false },
            title = { Text("Update Progress Photos") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Select photos to update the project status.", color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    Text("Before Photo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(Line).clickable { beforePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentAlignment = Alignment.Center) {
                        if (beforeUri != null) AsyncImage(model = beforeUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.AddPhotoAlternate, null, tint = BrandBlue)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text("After/Progress Photo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(Line).clickable { afterPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentAlignment = Alignment.Center) {
                        if (afterUri != null) AsyncImage(model = afterUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.AddAPhoto, null, tint = BrandBlue)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    onUpdateImages(beforeUri, afterUri)
                    showUpdatePhotosDialog = false 
                }) { Text("Update Photos", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showUpdatePhotosDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCompleteDialog) {
        var updateText by remember { mutableStateOf("") }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedImageUri = it }

        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Project") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Upload an 'After' photo and describe the completed work.", color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)).background(Line).clickable { pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = BrandBlue)
                                Text("Add After Photo", fontSize = 12.sp, color = BrandBlue)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    FieldLabel("Completion Summary")
                    Input(updateText, { updateText = it }, "e.g., Roof successfully replaced...", singleLine = false)
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (updateText.isNotBlank()) { onComplete(updateText, selectedImageUri); showCompleteDialog = false }
                }) { Text("Submit Project", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showCompleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CreateNeedScreen(onBack: () -> Unit, onSuggest: (String, String, NeedCategory, (Long, String) -> Unit) -> Unit, onCreated: (String, String, NeedCategory, Long, String, Uri?) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(NeedCategory.Roof) }
    var cost by rememberSaveable { mutableStateOf("") }
    var reason by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedImageUri = it }

    AppScaffold(title = "Publish Need", showBottomBar = false, onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
            FieldLabel("Before Photo (Required)")
            Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(Line).clickable { pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentAlignment = Alignment.Center) {
                if (selectedImageUri != null) {
                    AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.align(Alignment.TopEnd).padding(8.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).clickable { selectedImageUri = null }.padding(4.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(40.dp), tint = BrandBlue)
                        Text("Select Before Image", fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                }
            }

            FieldLabel("What is required?")
            Input(title, { title = it }, "e.g., Computer Lab Restoration", leadingIcon = Icons.Default.Edit)
            
            FieldLabel("Provide Details")
            Input(description, { description = it }, "Explain the current condition...", singleLine = false, leadingIcon = Icons.Default.Description)
            
            FieldLabel("Category")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeedCategory.entries.filter { it != NeedCategory.All }.forEach { 
                    FilterChip(it.label, category == it) { category = it } 
                }
            }
            
            FieldLabel("Estimated Budget (Rs.)")
            Input(cost, { cost = it }, "0.00", KeyboardType.Number, leadingIcon = Icons.Default.Payments)
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSuggest(title, description, category) { suggestedCost, suggestedReason -> cost = suggestedCost.toString(); reason = suggestedReason } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue.copy(alpha = 0.1f), contentColor = BrandBlue)
            ) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("AI Cost Estimator", fontWeight = FontWeight.Bold)
            }
            
            if (reason.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().background(Success.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Text(reason, fontSize = 13.sp, color = Success)
                }
            }

            Spacer(Modifier.height(32.dp))
            PrimaryButton(
                text = "Publish to Alumni",
                enabled = title.isNotBlank() && cost.isNotBlank() && selectedImageUri != null
            ) {
                onCreated(title, description, category, cost.toLongOrNull() ?: 0L, reason, selectedImageUri)
            }
            if (selectedImageUri == null) {
                Text("* Before photo is required to publish", color = Color.Red.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun SchoolUpdatesScreen(needs: List<SchoolNeed>, onTab: (AppScreen) -> Unit) {
    val completedNeeds = needs.filter { it.status == NeedStatus.Completed }
    AppScaffold(title = "School Updates", selected = AppScreen.Reviews, onTab = onTab) {
        if (completedNeeds.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No project updates yet.", color = Muted)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(completedNeeds) { need ->
                    UpdateCard(need)
                }
            }
        }
    }
}

@Composable
private fun UpdateCard(need: SchoolNeed) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(model = need.afterPhotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.align(Alignment.BottomEnd).padding(12.dp).background(Success, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("COMPLETED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
            Column(Modifier.padding(16.dp)) {
                Text(need.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(need.schoolName, color = BrandAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text(need.completionUpdate ?: "", color = Muted, fontSize = 14.sp, maxLines = 3)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Impact: ${currency(need.estimatedCost)} utilized", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Success)
                }
            }
        }
    }
}

@Composable
private fun FameScreen(entries: List<LeaderboardEntry>, onTab: (AppScreen) -> Unit) {
    AppScaffold(title = "Hall of Fame", selected = AppScreen.Fame, onTab = onTab) {
        Column {
            Box(Modifier.fillMaxWidth().background(BrandGradient).padding(24.dp)) {
                Column {
                    Text("Top Contributors", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Honoring those who lead the change", color = Color.White.copy(alpha = 0.8f))
                }
            }
            LazyColumn(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(entries.take(10).withIndex().toList()) { (index, entry) ->
                    FameRow(index + 1, entry)
                }
            }
        }
    }
}

@Composable
private fun FameRow(rank: Int, entry: LeaderboardEntry) {
    val rankColor = when(rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Line
    }
    Row(
        Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(rankColor), contentAlignment = Alignment.Center) {
            Text(rank.toString(), fontWeight = FontWeight.Bold, color = if(rank <= 3) Color.White else Muted)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Diamond Contributor", color = Muted, fontSize = 12.sp)
        }
        Text(currency(entry.totalPledged), fontWeight = FontWeight.ExtraBold, color = BrandBlue)
    }
}

@Composable
private fun ProfileScreen(phone: String, userName: String, role: UserRole, photoUrl: String?, onUpdateName: (String) -> Unit, onUpdateImage: (Uri) -> Unit, onSignOut: () -> Unit, onTab: (AppScreen) -> Unit) {
    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onUpdateImage(uri)
    }
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userName) }

    AppScaffold(title = "Settings", selected = AppScreen.Profile, onTab = onTab) {
        Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(80.dp).clip(CircleShape).background(Line)
                    .clickable { pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl != null) {
                        AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, tint = BrandBlue, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    if (editingName) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { onUpdateName(tempName); editingName = false }) {
                                    Icon(Icons.Default.Check, null, tint = Success)
                                }
                            }
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userName.ifBlank { "Add Name" }, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            IconButton(onClick = { editingName = true }) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Muted)
                            }
                        }
                    }
                    Text(phone.ifBlank { "No Phone Linked" }, color = Muted, fontSize = 14.sp)
                    Text(role.name, color = BrandAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            
            Spacer(Modifier.height(40.dp))
            SectionTitle("Account Settings")
            SettingItem(Icons.Default.Notifications, "Notifications", "Alerts for pledges and updates")
            SettingItem(Icons.Default.Security, "Privacy & Security", "Manage your data and account")
            SettingItem(Icons.AutoMirrored.Filled.Help, "Help Center", "FAQ and support contact")
            SettingItem(Icons.Default.Info, "About Us", "Our mission and team")

            Spacer(Modifier.height(40.dp))
            
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Logout from Account", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().clickable { }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Line)
    }
    HorizontalDivider(color = Line.copy(alpha = 0.5f))
}

@Composable
private fun MyPledgesScreen(pledges: List<Pledge>, onTab: (AppScreen) -> Unit) {
    AppScaffold(title = "My Pledges", selected = AppScreen.Pledges, onTab = onTab) {
        if (pledges.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, null, modifier = Modifier.size(64.dp), tint = Line)
                    Spacer(Modifier.height(16.dp))
                    Text("You haven't made any pledges yet. Start by supporting a school!", color = Muted, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("Your Impact History", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Showing your contributions across all schools", color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                }
                items(pledges) { pledge ->
                    PledgeHistoryCard(pledge)
                }
            }
        }
    }
}

@Composable
private fun PledgeHistoryCard(pledge: Pledge) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Line)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Favorite, null, tint = BrandBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(pledge.schoolName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (pledge.note.isNotBlank()) {
                    Text("\"${pledge.note}\"", color = Muted, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
                Text(currency(pledge.amount), fontWeight = FontWeight.ExtraBold, color = Success, fontSize = 15.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Pledged", color = Muted, fontSize = 11.sp)
                Text(pledge.createdAt?.let { "Today" } ?: "", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun DetailChip(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BrandBlue.copy(alpha = 0.05f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, null, tint = BrandBlue, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BrandBlue)
    }
}

@Composable
private fun StatusChip(status: NeedStatus) {
    val color = when(status) {
        NeedStatus.Open -> BrandAccent
        NeedStatus.InProgress -> Color(0xFFFFB23D)
        NeedStatus.Completed -> Success
    }
    Box(Modifier.clip(CircleShape).background(color.copy(alpha = 0.1f)).border(1.dp, color, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(status.label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AppScaffold(title: String, selected: AppScreen = AppScreen.Home, showBottomBar: Boolean = true, onBack: (() -> Unit)? = null, onTab: (AppScreen) -> Unit = {}, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            }
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (onBack == null) {
                IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null) }
            }
        }
        Box(Modifier.weight(1f)) { content() }
        if (showBottomBar) {
            HorizontalDivider(color = Line)
            BottomBar(selected, onTab)
        }
    }
}

@Composable
private fun BottomBar(selected: AppScreen, onTab: (AppScreen) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf(
            Triple("Home", Icons.Default.Dashboard, AppScreen.Home),
            Triple("Pledges", Icons.Default.FavoriteBorder, AppScreen.Pledges),
            Triple("Fame", Icons.Default.EmojiEvents, AppScreen.Fame),
            Triple("Updates", Icons.Default.Newspaper, AppScreen.Reviews),
            Triple("Profile", Icons.Default.PersonOutline, AppScreen.Profile)
        )
        items.forEach { (label, icon, screen) ->
            NavigationBarItem(
                selected = selected == screen,
                onClick = { onTab(screen) },
                icon = { Icon(icon, null) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandBlue, selectedTextColor = BrandBlue, unselectedIconColor = Muted, unselectedTextColor = Muted)
            )
        }
    }
}

@Composable private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) { 
    Text(
        label, 
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) BrandBlue else Color.White).border(1.dp, if (selected) BrandBlue else Line, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp), 
        color = if (selected) Color.White else Color.Black, 
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
    ) 
}

@Composable private fun ProgressBar(value: Float, height: androidx.compose.ui.unit.Dp = 6.dp) { 
    Box(Modifier.fillMaxWidth().height(height).clip(CircleShape).background(Line)) { 
        Box(Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).height(height).clip(CircleShape).background(if (value >= 1f) SuccessGradient else BrandGradient)) 
    } 
}

@Composable private fun PhotoBox(text: String = "Photo") { 
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { 
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Image, null, modifier = Modifier.size(48.dp), tint = Color.White.copy(alpha = 0.5f)) 
            Text(text, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    } 
}

@Composable private fun FieldLabel(label: String) { 
    Spacer(Modifier.height(16.dp))
    Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandBlue)
    Spacer(Modifier.height(8.dp)) 
}

@Composable private fun Input(
    value: String, 
    onChange: (String) -> Unit, 
    placeholder: String, 
    keyboardType: KeyboardType = KeyboardType.Text, 
    singleLine: Boolean = true, 
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) { 
    OutlinedTextField(
        value = value, 
        onValueChange = onChange, 
        placeholder = { Text(placeholder) }, 
        singleLine = singleLine, 
        minLines = if (singleLine) 1 else 4, 
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), 
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(12.dp),
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = BrandBlue) } },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
    ) 
}

@Composable private fun SectionTitle(text: String) { 
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlue, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(12.dp)) 
}

@Composable private fun PrimaryButton(text: String, color: Color = BrandBlue, enabled: Boolean = true, onClick: () -> Unit) { 
    Button(
        onClick = onClick, 
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(12.dp)), 
        shape = RoundedCornerShape(12.dp), 
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) { 
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp) 
    } 
}

private fun currency(amount: Long): String = "₹%,d".format(amount)

@Preview(showBackground = true)
@Composable
private fun PreviewApp() { ShaaleVikasApp() }
