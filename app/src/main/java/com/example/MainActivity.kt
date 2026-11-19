package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.authStateFlow
import com.example.auth.consumePendingSignUpDisplayName
import com.example.auth.signOutUser
import com.example.data.KarirKuRepository
import com.example.ui.components.DossierDetailBottomSheet
import com.example.ui.components.KarirKuBrandBadge
import com.example.ui.components.KarirKuTopBar
import com.example.ui.components.modernColoredShadow
import com.example.ui.components.tactileClickable
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.CatatanScreen
import com.example.ui.screens.DossierFormScreen
import com.example.ui.screens.InfoLowonganScreen
import com.example.ui.screens.JadwalScreen
import com.example.ui.screens.LamaranScreen
import com.example.ui.screens.ProfilScreen
import com.example.ui.theme.KarirKuTheme
import com.example.ui.theme.KarirKuThemeTokens
import com.example.ui.theme.MonoEyebrowStyle
import com.example.viewmodel.KarirKuViewModel
import com.example.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KarirKuAppRoot()
        }
    }
}

private enum class KarirKuTab(
    val route: String,
    val label: String,
    val subtitleTag: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    BERANDA("beranda", "Beranda", "CATATAN KARIER", Icons.Filled.Explore, Icons.Outlined.Explore),
    LAMARAN("lamaran", "Lamaran", "DAFTAR LAMARAN", Icons.Filled.Work, Icons.Outlined.WorkOutline),
    JADWAL("jadwal", "Jadwal", "AGENDA INTERVIEW", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    PROFIL("profil", "Profil", "PROFIL KARIER", Icons.Filled.Person, Icons.Outlined.Person)
}

private enum class SecondaryScreen {
    NONE,
    FORM,
    INFO_LOWONGAN,
    CATATAN
}

@Composable
fun KarirKuAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        KarirKuTheme(darkTheme = false) {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        key(user.uid) {
            AuthenticatedKarirKuShell(user = user)
        }
    }
}

@Composable
private fun AuthenticatedKarirKuShell(user: FirebaseUser) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: KarirKuViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                KarirKuViewModel(
                    repository = KarirKuRepository(db),
                    currentUserId = user.uid,
                    initialDisplayName = consumePendingSignUpDisplayName()
                        ?: user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                        ?: "Pengguna KarirKu",
                    initialPhotoUrl = user.photoUrl?.toString()
                )
            }
        }
    )

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val publicJobListingsState by viewModel.publicJobListingsState.collectAsStateWithLifecycle()
    val notesState by viewModel.notesState.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val darkThemeEnabled = dashboardState.profile?.darkModeEnabled ?: false
    val publicJobCount = (publicJobListingsState as? UiState.Success)?.data?.size ?: 0

    KarirKuTheme(darkTheme = darkThemeEnabled) {
        val colors = KarirKuThemeTokens.colors
        val snackbarHostState = remember { SnackbarHostState() }

        var activeTab by rememberSaveable { mutableStateOf(KarirKuTab.BERANDA) }
        var secondaryScreen by rememberSaveable { mutableStateOf(SecondaryScreen.NONE) }
        var editingApplicationId by rememberSaveable { mutableStateOf<String?>(null) }
        var inspectedApplicationId by rememberSaveable { mutableStateOf<String?>(null) }
        var pendingDeleteAppId by rememberSaveable { mutableStateOf<String?>(null) }
        var preselectedAppForScheduleId by rememberSaveable { mutableStateOf<String?>(null) }

        val isSecondaryOpen = secondaryScreen != SecondaryScreen.NONE

        // Return cleanly to Beranda when pressing Android Back on secondary tabs
        BackHandler(enabled = !isSecondaryOpen && activeTab != KarirKuTab.BERANDA) {
            activeTab = KarirKuTab.BERANDA
        }

        val editingApplication = remember(editingApplicationId, dashboardState.applications) {
            dashboardState.applications.firstOrNull { it.id == editingApplicationId }
        }
        val inspectedApplication = remember(inspectedApplicationId, dashboardState.applications) {
            dashboardState.applications.firstOrNull { it.id == inspectedApplicationId }
        }
        val preselectedAppForSchedule = remember(preselectedAppForScheduleId, dashboardState.applications) {
            dashboardState.applications.firstOrNull { it.id == preselectedAppForScheduleId }
        }

        val userInitials = remember(dashboardState.profile?.displayName, user.displayName) {
            val name = dashboardState.profile?.displayName?.ifBlank { user.displayName } ?: "KK"
            name.trim().split(Regex("\\s+")).take(2).joinToString("") { it.take(1) }.ifBlank { "KK" }
        }

        LaunchedEffect(viewModel) {
            viewModel.snackbarEvents.collect { msg ->
                snackbarHostState.showSnackbar(msg)
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.canvas)
        ) {
            val useNavigationRail = maxWidth >= 680.dp

            Scaffold(
                containerColor = colors.canvas,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useNavigationRail && !isSecondaryOpen) {
                        KarirKuFloatingBottomDock(
                            activeTab = activeTab,
                            onSelectTab = { activeTab = it }
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail && !isSecondaryOpen) {
                        KarirKuNavigationRail(
                            activeTab = activeTab,
                            onSelectTab = { activeTab = it }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxSize()
                        ) {
                            when (secondaryScreen) {
                                SecondaryScreen.FORM -> {
                                    DossierFormScreen(
                                        existingApplication = editingApplication,
                                        savedProfileDocuments = dashboardState.documents,
                                        expectedSalaryBaseline = dashboardState.profile?.expectedSalaryMin ?: 0L,
                                        isSaving = isSaving,
                                        onNavigateBack = {
                                            secondaryScreen = SecondaryScreen.NONE
                                            editingApplicationId = null
                                        },
                                        onSave = { exId, exCode, role, comp, loc, st, wm, ct, minS, maxS, appMs, tgtMs, notes, hr, atts, starred ->
                                            viewModel.saveApplication(
                                                existingId = exId,
                                                existingDossierCode = exCode,
                                                roleTitle = role,
                                                companyName = comp,
                                                location = loc,
                                                status = st,
                                                workMode = wm,
                                                contractType = ct,
                                                salaryMin = minS,
                                                salaryMax = maxS,
                                                appliedDateMillis = appMs,
                                                targetResponseMillis = tgtMs,
                                                notes = notes,
                                                hrContact = hr,
                                                attachments = atts,
                                                starred = starred,
                                                onSuccess = {
                                                    secondaryScreen = SecondaryScreen.NONE
                                                    editingApplicationId = null
                                                }
                                            )
                                        }
                                    )
                                }

                                SecondaryScreen.INFO_LOWONGAN -> {
                                    InfoLowonganScreen(
                                        listingsState = publicJobListingsState,
                                        currentUserId = user.uid,
                                        isSaving = isSaving,
                                        onNavigateBack = { secondaryScreen = SecondaryScreen.NONE },
                                        onSaveListing = viewModel::savePublicJobListing,
                                        onDeleteListing = viewModel::deletePublicJobListing
                                    )
                                }

                                SecondaryScreen.CATATAN -> {
                                    CatatanScreen(
                                        notesState = notesState,
                                        isSaving = isSaving,
                                        onNavigateBack = { secondaryScreen = SecondaryScreen.NONE },
                                        onSaveNote = viewModel::saveNote,
                                        onTogglePin = viewModel::toggleNotePinned,
                                        onDeleteNote = viewModel::deleteNote
                                    )
                                }

                                SecondaryScreen.NONE -> {
                                KarirKuTopBar(
                                    subtitleTag = activeTab.subtitleTag,
                                    userPhotoUrl = dashboardState.profile?.photoUrl ?: user.photoUrl?.toString(),
                                    userInitials = userInitials,
                                    hasUpcomingAlert = (dashboardState.profile?.followUpRemindersEnabled != false) &&
                                        dashboardState.metrics.upcomingInterviewsThisWeek > 0,
                                    onNotificationClick = { activeTab = KarirKuTab.JADWAL },
                                    onProfileClick = { activeTab = KarirKuTab.PROFIL }
                                )

                                if (dashboardState.errorMessage != null) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = colors.crimsonSoftBg,
                                        border = BorderStroke(1.dp, colors.crimsonBorder)
                                    ) {
                                        Text(
                                            text = dashboardState.errorMessage!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.accentCrimson,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (dashboardState.isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(color = colors.textPrimary)
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                text = "MEMUAT DATA...",
                                                style = MonoEyebrowStyle,
                                                color = colors.textMuted
                                            )
                                        }
                                    }
                                } else {
                                    Crossfade(
                                        targetState = activeTab,
                                        animationSpec = tween(220),
                                        modifier = Modifier.weight(1f),
                                        label = "tab_crossfade"
                                    ) { tab ->
                                        when (tab) {
                                            KarirKuTab.BERANDA -> BerandaScreen(
                                                state = dashboardState,
                                                publicJobCount = publicJobCount,
                                                selectedFilter = selectedStatusFilter,
                                                sortOrder = sortOrder,
                                                onSelectFilter = viewModel::setStatusFilter,
                                                onCycleSort = viewModel::cycleSortOrder,
                                                onOpenApplicationDetail = { app ->
                                                    inspectedApplicationId = app.id
                                                },
                                                onToggleStar = viewModel::toggleApplicationStar,
                                                onAddApplicationClick = {
                                                    editingApplicationId = null
                                                    secondaryScreen = SecondaryScreen.FORM
                                                },
                                                onOpenScheduleTab = { activeTab = KarirKuTab.JADWAL },
                                                onOpenInfoLowongan = {
                                                    secondaryScreen = SecondaryScreen.INFO_LOWONGAN
                                                },
                                                onOpenCatatan = {
                                                    secondaryScreen = SecondaryScreen.CATATAN
                                                },
                                                onSoonFeatureClick = { featureName ->
                                                    viewModel.showMessage("$featureName sedang disiapkan dan segera hadir!")
                                                }
                                            )

                                            KarirKuTab.LAMARAN -> LamaranScreen(
                                                state = dashboardState,
                                                searchQuery = searchQuery,
                                                selectedFilter = selectedStatusFilter,
                                                onSearchQueryChange = viewModel::setSearchQuery,
                                                onSelectFilter = viewModel::setStatusFilter,
                                                onOpenApplicationDetail = { app ->
                                                    inspectedApplicationId = app.id
                                                },
                                                onToggleStar = viewModel::toggleApplicationStar,
                                                onAddNewDossierClick = {
                                                    editingApplicationId = null
                                                    secondaryScreen = SecondaryScreen.FORM
                                                }
                                            )

                                            KarirKuTab.JADWAL -> JadwalScreen(
                                                state = dashboardState,
                                                preselectedApplicationForNewSchedule = preselectedAppForSchedule,
                                                onClearPreselectedApplication = {
                                                    preselectedAppForScheduleId = null
                                                },
                                                isSaving = isSaving,
                                                onSaveSchedule = viewModel::saveSchedule,
                                                onToggleCompleted = viewModel::toggleScheduleCompleted,
                                                onDeleteSchedule = viewModel::deleteSchedule
                                            )

                                            KarirKuTab.PROFIL -> ProfilScreen(
                                                state = dashboardState,
                                                userEmail = user.email,
                                                fallbackPhotoUrl = user.photoUrl?.toString(),
                                                fallbackInitials = userInitials,
                                                isSaving = isSaving,
                                                onUpdateIdentity = viewModel::updateProfileIdentity,
                                                onUpdatePreferences = viewModel::updateCareerPreferences,
                                                onUpdateSettings = viewModel::updateSettings,
                                                onSaveDocument = viewModel::saveDocument,
                                                onDeleteDocument = viewModel::deleteDocument,
                                                onSignOutClick = {
                                                    signOutUser(
                                                        credentialManager = credentialManager,
                                                        scope = scope
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (inspectedApplication != null) {
            DossierDetailBottomSheet(
                application = inspectedApplication,
                onDismiss = { inspectedApplicationId = null },
                onStatusChange = { newStatus ->
                    viewModel.updateApplicationStatus(inspectedApplication.id, newStatus)
                },
                onEditClick = {
                    editingApplicationId = inspectedApplication.id
                    inspectedApplicationId = null
                    secondaryScreen = SecondaryScreen.FORM
                },
                onCreateInterviewClick = {
                    preselectedAppForScheduleId = inspectedApplication.id
                    inspectedApplicationId = null
                    activeTab = KarirKuTab.JADWAL
                },
                onDeleteClick = {
                    pendingDeleteAppId = inspectedApplication.id
                    inspectedApplicationId = null
                }
            )
        }

        if (pendingDeleteAppId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteAppId = null },
                title = { Text("Hapus Lamaran Ini?") },
                text = { Text("Data lamaran ini akan dihapus secara permanen dari catatan KarirKu.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idToDelete = pendingDeleteAppId!!
                            pendingDeleteAppId = null
                            viewModel.deleteApplication(idToDelete)
                        }
                    ) {
                        Text("Hapus", color = colors.accentCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteAppId = null }) { Text("Batal") }
                }
            )
        }
    }
}

/**
 * Gojek-inspired sculpted floating navigation dock with soft diffused shadow,
 * glossy top rim, tactile spring active pill, and full navigation bar inset support.
 */
@Composable
private fun KarirKuFloatingBottomDock(
    activeTab: KarirKuTab,
    onSelectTab: (KarirKuTab) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .modernColoredShadow(
                    color = if (colors.isDark) Color.Black.copy(alpha = 0.55f) else Color(0xFF1F1912).copy(alpha = 0.16f),
                    cornerRadius = 26.dp,
                    blurRadius = 22.dp,
                    offsetY = 8.dp
                )
                .clip(RoundedCornerShape(26.dp))
                .background(colors.cardSurface)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (colors.isDark) 0.14f else 0.95f),
                            colors.borderSubtle
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarirKuTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    val pillBg by animateColorAsState(
                        targetValue = if (selected) colors.heroDarkSurface else Color.Transparent,
                        animationSpec = tween(200),
                        label = "nav_pill_bg"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (selected) colors.heroOnDark else colors.textMuted,
                        animationSpec = tween(200),
                        label = "nav_icon_tint"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .tactileClickable(pressedScale = 0.94f) { onSelectTab(tab) }
                            .padding(vertical = 4.dp)
                            .testTag("nav_tab_${tab.route}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(pillBg)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                tint = iconTint,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Text(
                            text = tab.label.uppercase(),
                            style = MonoEyebrowStyle.copy(
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (selected) colors.textPrimary else colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KarirKuNavigationRail(
    activeTab: KarirKuTab,
    onSelectTab: (KarirKuTab) -> Unit
) {
    val colors = KarirKuThemeTokens.colors
    NavigationRail(
        containerColor = colors.cardSurface,
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding(),
        header = {
            KarirKuBrandBadge(
                size = 40.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    ) {
        KarirKuTab.entries.forEach { tab ->
            val selected = activeTab == tab
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                modifier = Modifier.testTag("rail_tab_${tab.route}"),
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.heroOnDark,
                    selectedTextColor = colors.textPrimary,
                    indicatorColor = colors.heroDarkSurface,
                    unselectedIconColor = colors.textMuted,
                    unselectedTextColor = colors.textMuted
                )
            )
        }
    }
}
