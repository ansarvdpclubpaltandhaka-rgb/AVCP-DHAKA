package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.AppConstants
import com.example.core.UiState
import com.example.data.DemoDataProvider
import com.example.data.OrgRepository
import com.example.model.UserProfile
import com.example.ui.components.OrgHeaderBar
import com.example.ui.screens.*
import com.example.ui.theme.AnsarVdpTheme
import com.example.viewmodel.AuthSessionState
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MainViewModel

object Routes {
    const val HOME = "home"
    const val ATTENDANCE = "attendance"
    const val SERVICES = "services"
    const val ACTIVITIES = "activities"
    const val NOTICES = "notices"
    const val PROFILE = "profile"
    const val AUTH = "auth?admin={admin}"
    const val MEMBER_AUTH = "member_auth?register={register}"
    const val BLOOD_DONATION = "blood_donation"
    const val DIRECTORY = "directory?tab={tab}"
    const val ALLOWANCE_DETAILS = "allowance_details"
    const val MESSAGES = "messages"
    const val CHAT = "chat/{conversationId}"
    const val NOTIFICATIONS = "notifications"
    const val FINANCE = "finance"
    const val ADMIN = "admin"

    fun chatRoute(conversationId: String) = "chat/$conversationId"
    fun directoryRoute(tab: Int = 0) = "directory?tab=$tab"
    fun authRoute(admin: Boolean = false) = "auth?admin=$admin"
    fun memberAuthRoute(register: Boolean = false) = "member_auth?register=$register"
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val pendingMemberBottomTabs = listOf(
    BottomNavTab(Routes.HOME, "ড্যাশবোর্ড", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    BottomNavTab(Routes.PROFILE, "সেটিংস", Icons.Rounded.Settings, Icons.Outlined.Settings)
)

private val memberBottomTabs = listOf(
    BottomNavTab(Routes.HOME, "ড্যাশবোর্ড", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    BottomNavTab(Routes.ATTENDANCE, "উপস্থিতি", Icons.Rounded.HowToReg, Icons.Outlined.HowToReg),
    BottomNavTab(Routes.NOTICES, "নোটিশ", Icons.Rounded.Campaign, Icons.Outlined.Campaign),
    BottomNavTab(Routes.PROFILE, "সেটিংস", Icons.Rounded.Settings, Icons.Outlined.Settings)
)

private val adminBottomTabs = listOf(
    BottomNavTab(Routes.HOME, "ড্যাশবোর্ড", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    BottomNavTab(Routes.ATTENDANCE, "উপস্থিতি", Icons.Rounded.HowToReg, Icons.Outlined.HowToReg),
    BottomNavTab(Routes.SERVICES, "সেবা", Icons.Rounded.Handshake, Icons.Outlined.Handshake),
    BottomNavTab(Routes.NOTICES, "নোটিশ", Icons.Rounded.Campaign, Icons.Outlined.Campaign),
    BottomNavTab(Routes.PROFILE, "সেটিংস", Icons.Rounded.Settings, Icons.Outlined.Settings)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnsarVdpTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot(authViewModel: AuthViewModel = viewModel()) {
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()

    val isAuthenticated = sessionState is AuthSessionState.Authenticated
    val resolvedUser: UserProfile = (sessionState as? AuthSessionState.Authenticated)?.user
        ?: DemoDataProvider.demoGuestProfile

    key("${resolvedUser.uid}_$isAuthenticated") {
        MainAppShell(
            currentUser = resolvedUser,
            isAuthenticated = isAuthenticated,
            authViewModel = authViewModel
        )
    }
}

@Composable
private fun MainAppShell(
    currentUser: UserProfile,
    isAuthenticated: Boolean,
    authViewModel: AuthViewModel
) {
    val isSuperAdmin = isAuthenticated && (
        AppConstants.isSuperAdminUser(currentUser.uid, currentUser.email) ||
            currentUser.role == AppConstants.ROLE_SUPER_ADMIN
        )
    val isAdmin = isAuthenticated && (isSuperAdmin || currentUser.role == AppConstants.ROLE_ADMIN) &&
        currentUser.status == "active"
    val isMemberPending = isAuthenticated && !isAdmin &&
        (!AppConstants.isApprovedMemberStatus(currentUser.status) || currentUser.trade.isBlank())

    val mainViewModel: MainViewModel = viewModel(
        key = "${currentUser.uid}_${currentUser.role}_${currentUser.name}_$isAuthenticated",
        factory = viewModelFactory {
            initializer {
                MainViewModel(
                    repository = OrgRepository(),
                    currentUid = currentUser.uid,
                    isAuthenticated = isAuthenticated,
                    isAdmin = isAdmin,
                    isSuperAdmin = isSuperAdmin,
                    currentUserName = currentUser.name,
                    currentUserPhone = currentUser.phone
                )
            }
        }
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val feedback by mainViewModel.actionFeedback.collectAsStateWithLifecycle()
    val authFeedback by authViewModel.authMessage.collectAsStateWithLifecycle()
    val isAuthSuccess by authViewModel.isSuccessMessage.collectAsStateWithLifecycle()
    val isBusy by authViewModel.isBusy.collectAsStateWithLifecycle()
    val registeredAccounts by authViewModel.registeredAccounts.collectAsStateWithLifecycle()

    LaunchedEffect(registeredAccounts) {
        mainViewModel.syncRegisteredMembers(registeredAccounts)
    }

    LaunchedEffect(Unit) {
        authViewModel.loginSucceededEvent.collect {
            val route = navController.currentDestination?.route
            if (route?.startsWith("auth") == true || route?.startsWith("member_auth") == true) {
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(feedback) {
        if (!feedback.isNullOrBlank()) {
            snackbarHostState.showSnackbar(feedback!!)
            mainViewModel.clearFeedback()
        }
    }
    LaunchedEffect(authFeedback) {
        if (!authFeedback.isNullOrBlank() &&
            currentRoute?.startsWith("auth") != true &&
            currentRoute?.startsWith("member_auth") != true
        ) {
            snackbarHostState.showSnackbar(authFeedback!!)
            authViewModel.clearMessage()
        }
    }

    val orgConfig by mainViewModel.orgConfig.collectAsStateWithLifecycle()
    val directoryState by mainViewModel.directoryState.collectAsStateWithLifecycle()
    val positionsState by mainViewModel.positionsState.collectAsStateWithLifecycle()
    val servicesState by mainViewModel.servicesState.collectAsStateWithLifecycle()
    val activitiesState by mainViewModel.activitiesState.collectAsStateWithLifecycle()
    val bloodRequestsState by mainViewModel.bloodRequestsState.collectAsStateWithLifecycle()
    val noticesState by mainViewModel.noticesState.collectAsStateWithLifecycle()
    val eventsState by mainViewModel.eventsState.collectAsStateWithLifecycle()
    val allowancesState by mainViewModel.allowancesState.collectAsStateWithLifecycle()
    val myServiceRequestsState by mainViewModel.myServiceRequestsState.collectAsStateWithLifecycle()
    val myMedicalCasesState by mainViewModel.myMedicalCasesState.collectAsStateWithLifecycle()
    val myContributionsState by mainViewModel.myContributionsState.collectAsStateWithLifecycle()
    val conversationsState by mainViewModel.conversationsState.collectAsStateWithLifecycle()
    val notificationsState by mainViewModel.notificationsState.collectAsStateWithLifecycle()
    val activeMessagesState by mainViewModel.activeMessagesState.collectAsStateWithLifecycle()

    val adminMembersState by mainViewModel.adminMembersState.collectAsStateWithLifecycle()
    val adminVolunteersState by mainViewModel.adminVolunteersState.collectAsStateWithLifecycle()
    val adminContributionsState by mainViewModel.adminContributionsState.collectAsStateWithLifecycle()
    val adminDonationsState by mainViewModel.adminDonationsState.collectAsStateWithLifecycle()
    val adminExpensesState by mainViewModel.adminExpensesState.collectAsStateWithLifecycle()
    val financialSummary by mainViewModel.financialSummary.collectAsStateWithLifecycle()

    val unreadNotifications = remember(notificationsState, currentUser.uid) {
        val list = (notificationsState as? UiState.Success)?.data.orEmpty()
        list.count { currentUser.uid !in it.readBy }
    }

    val approveMemberAndSync: (String, String) -> Unit = { memberId, trade ->
        authViewModel.approveMemberAndSetTrade(memberId, trade)
        mainViewModel.approveMemberWithTrade(memberId, trade)
    }
    val rejectMemberAndSync: (String) -> Unit = { memberId ->
        authViewModel.rejectMember(memberId)
        mainViewModel.rejectMember(memberId)
    }
    val deleteMemberAndSync: (String) -> Unit = { memberId ->
        authViewModel.removeRegisteredMember(memberId)
        mainViewModel.deleteMember(memberId)
    }

    val activeTabs = when {
        isAdmin -> adminBottomTabs
        isMemberPending -> pendingMemberBottomTabs
        else -> memberBottomTabs
    }
    val topLevelRoutes = remember(isAdmin, isMemberPending) { activeTabs.map { it.route }.toSet() }
    val showMainChrome = currentRoute in topLevelRoutes

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showMainChrome) {
                OrgHeaderBar(
                    orgName = orgConfig?.name?.ifBlank { AppConstants.ORG_NAME_BN } ?: AppConstants.ORG_NAME_BN,
                    isAuthenticated = isAuthenticated,
                    unreadNotifications = unreadNotifications,
                    userPhotoUrl = currentUser.photoURL,
                    isAdmin = isAdmin,
                    onLoginClick = { navController.navigate(Routes.authRoute(true)) },
                    onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                    onMessagesClick = { navController.navigate(Routes.MESSAGES) },
                    onProfileClick = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAdminClick = {
                        if (isAdmin) navController.navigate(Routes.ADMIN)
                    }
                )
            }
        },
        bottomBar = {
            if (showMainChrome) {
                NavigationBar {
                    activeTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label, maxLines = 1) },
                            modifier = Modifier.testTag("nav_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        currentUser = currentUser,
                        isAuthenticated = isAuthenticated,
                        isAdmin = isAdmin,
                        orgConfig = orgConfig,
                        directoryState = directoryState,
                        servicesState = servicesState,
                        activitiesState = activitiesState,
                        bloodRequestsState = bloodRequestsState,
                        noticesState = noticesState,
                        allowancesState = allowancesState,
                        onNavigateMemberLogin = { navController.navigate(Routes.memberAuthRoute(false)) },
                        onNavigateAdminLogin = { navController.navigate(Routes.authRoute(true)) },
                        onMemberSignOut = { authViewModel.signOut() },
                        onNavigateServices = { if (isAdmin) navController.navigate(Routes.SERVICES) },
                        onNavigateBloodDonation = { if (isAdmin) navController.navigate(Routes.BLOOD_DONATION) },
                        onNavigateActivities = { if (isAdmin) navController.navigate(Routes.ACTIVITIES) },
                        onNavigateDirectory = {
                            if (!isMemberPending) navController.navigate(Routes.directoryRoute(0))
                        },
                        onNavigateFieldDuty = {
                            if (!isMemberPending) navController.navigate(Routes.directoryRoute(1))
                        },
                        onNavigateAllowanceDetails = {
                            if (!isMemberPending) navController.navigate(Routes.ALLOWANCE_DETAILS)
                        },
                        onNavigateNotices = {
                            if (!isMemberPending) {
                                navController.navigate(Routes.NOTICES) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }

                composable(Routes.ATTENDANCE) {
                    DirectoryScreen(
                        currentUser = currentUser,
                        isAuthenticated = isAuthenticated,
                        isAdmin = isAdmin,
                        directoryState = directoryState,
                        positionsState = positionsState,
                        adminMembersState = adminMembersState,
                        adminVolunteersState = adminVolunteersState,
                        initialTab = 0,
                        showTopBackButton = false,
                        onBack = {},
                        onAddMember = { name, phone, email, bg, pos, addr, platoon, trade ->
                            mainViewModel.saveMember(
                                existingId = null, linkedUid = "", name = name, photoUrl = "",
                                phone = phone, email = email, bloodGroup = bg, position = pos,
                                joiningDate = "", address = addr, status = AppConstants.STATUS_APPROVED,
                                bloodDonor = false, donorAvailable = false, donationCount = 0L, lastDonation = "",
                                platoon = platoon, trade = trade
                            )
                        },
                        onApproveMemberWithTrade = approveMemberAndSync,
                        onRejectMember = rejectMemberAndSync,
                        onDeleteMember = deleteMemberAndSync,
                        onMessageUser = { uid, name, photo ->
                            mainViewModel.startChatWith(currentUser, uid, name, photo) { convId ->
                                navController.navigate(Routes.chatRoute(convId))
                            }
                        }
                    )
                }

                composable(Routes.ALLOWANCE_DETAILS) {
                    if (isMemberPending) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        AllowanceDetailsScreen(
                            currentUser = currentUser,
                            isAuthenticated = isAuthenticated,
                            isAdmin = isAdmin,
                            allowancesState = allowancesState,
                            onBack = { navController.popBackStack() },
                            onEditAllowance = mainViewModel::updateAllowanceRecord
                        )
                    }
                }

                composable(
                    route = Routes.AUTH,
                    arguments = listOf(navArgument("admin") {
                        type = NavType.BoolType
                        defaultValue = false
                    })
                ) { entry ->
                    val isAdminLogin = entry.arguments?.getBoolean("admin") ?: true
                    AuthScreen(
                        isBusy = isBusy,
                        authMessage = authFeedback,
                        isAdminLoginIntent = isAdminLogin,
                        onClearMessage = authViewModel::clearMessage,
                        onSignIn = authViewModel::signIn,
                        onBackToDashboard = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Routes.MEMBER_AUTH,
                    arguments = listOf(navArgument("register") {
                        type = NavType.BoolType
                        defaultValue = false
                    })
                ) { entry ->
                    val initialRegister = entry.arguments?.getBoolean("register") ?: false
                    MemberAuthScreen(
                        isBusy = isBusy,
                        authMessage = authFeedback,
                        isSuccessMessage = isAuthSuccess,
                        initialRegisterMode = initialRegister,
                        memberRegisteredEvent = authViewModel.memberRegisteredEvent,
                        onClearMessage = authViewModel::clearMessage,
                        onMemberSignIn = authViewModel::signInMember,
                        onMemberRegister = authViewModel::registerMember,
                        onBackToDashboard = { navController.popBackStack() }
                    )
                }

                composable(Routes.SERVICES) {
                    if (isAdmin) {
                        ServicesScreen(
                            currentUser = currentUser,
                            servicesState = servicesState,
                            myRequestsState = myServiceRequestsState,
                            myMedicalCasesState = myMedicalCasesState,
                            onOpenBloodDonation = { navController.navigate(Routes.BLOOD_DONATION) },
                            onSubmitServiceRequest = mainViewModel::submitServiceRequest,
                            onSubmitMedicalCase = mainViewModel::submitMedicalCase
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(Routes.ACTIVITIES) {
                    if (isAdmin) {
                        ActivitiesScreen(activitiesState = activitiesState)
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(Routes.NOTICES) {
                    if (isMemberPending) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        NoticesScreen(
                            isAdmin = isAdmin,
                            noticesState = noticesState,
                            eventsState = eventsState,
                            onSendNotice = mainViewModel::createNotice,
                            onDeleteNotice = mainViewModel::deleteNotice
                        )
                    }
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        user = currentUser,
                        isAuthenticated = isAuthenticated,
                        orgConfig = orgConfig,
                        isAdmin = isAdmin,
                        isSuperAdmin = isSuperAdmin,
                        onMemberLoginClick = { navController.navigate(Routes.memberAuthRoute(false)) },
                        onAdminLoginClick = {
                            if (isAuthenticated && !isAdmin) {
                                authViewModel.signOut()
                            }
                            navController.navigate(Routes.authRoute(true))
                        },
                        onUpdateProfile = { name, phone, photoUrl, bg ->
                            authViewModel.updateOwnProfile(currentUser.uid, name, phone, photoUrl, bg)
                        },
                        onOpenDirectory = { navController.navigate(Routes.directoryRoute(0)) },
                        onOpenAllowanceDetails = { navController.navigate(Routes.ALLOWANCE_DETAILS) },
                        onOpenNotices = {
                            navController.navigate(Routes.NOTICES) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onOpenBloodDonation = { navController.navigate(Routes.BLOOD_DONATION) },
                        onOpenMessages = { navController.navigate(Routes.MESSAGES) },
                        onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                        onOpenFinance = { navController.navigate(Routes.FINANCE) },
                        onOpenAdminPanel = { if (isAdmin) navController.navigate(Routes.ADMIN) },
                        onSignOut = { authViewModel.signOut() }
                    )
                }

                composable(Routes.BLOOD_DONATION) {
                    if (isAdmin) {
                        BloodDonationScreen(
                            currentUser = currentUser,
                            isAdmin = isAdmin,
                            directoryState = directoryState,
                            adminMembersState = adminMembersState,
                            bloodRequestsState = bloodRequestsState,
                            onBack = { navController.popBackStack() },
                            onCreateBloodRequest = mainViewModel::createBloodRequest,
                            onUpdateBloodStatus = mainViewModel::updateBloodRequestStatus,
                            onUpdateOwnDonorSettings = { donor, avail, count, lastDate, bg ->
                                mainViewModel.saveMember(
                                    existingId = null,
                                    linkedUid = currentUser.uid,
                                    name = currentUser.name,
                                    photoUrl = currentUser.photoURL,
                                    phone = currentUser.phone,
                                    email = currentUser.email,
                                    bloodGroup = bg,
                                    position = "সমন্বয়কারী",
                                    joiningDate = "",
                                    address = "পল্টন, ঢাকা",
                                    status = "active",
                                    bloodDonor = donor,
                                    donorAvailable = avail,
                                    donationCount = count,
                                    lastDonation = lastDate
                                )
                            },
                            onMessageUser = { uid, name, photo ->
                                mainViewModel.startChatWith(currentUser, uid, name, photo) { convId ->
                                    navController.navigate(Routes.chatRoute(convId))
                                }
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(
                    route = Routes.DIRECTORY,
                    arguments = listOf(navArgument("tab") {
                        type = NavType.IntType
                        defaultValue = 0
                    })
                ) { entry ->
                    val initialTab = entry.arguments?.getInt("tab") ?: 0
                    DirectoryScreen(
                        currentUser = currentUser,
                        isAuthenticated = isAuthenticated,
                        isAdmin = isAdmin,
                        directoryState = directoryState,
                        positionsState = positionsState,
                        adminMembersState = adminMembersState,
                        adminVolunteersState = adminVolunteersState,
                        initialTab = initialTab,
                        showTopBackButton = true,
                        onBack = { navController.popBackStack() },
                        onAddMember = { name, phone, email, bg, pos, addr, platoon, trade ->
                            mainViewModel.saveMember(
                                existingId = null, linkedUid = "", name = name, photoUrl = "",
                                phone = phone, email = email, bloodGroup = bg, position = pos,
                                joiningDate = "", address = addr, status = AppConstants.STATUS_APPROVED,
                                bloodDonor = false, donorAvailable = false, donationCount = 0L, lastDonation = "",
                                platoon = platoon, trade = trade
                            )
                        },
                        onApproveMemberWithTrade = approveMemberAndSync,
                        onRejectMember = rejectMemberAndSync,
                        onDeleteMember = deleteMemberAndSync,
                        onMessageUser = { uid, name, photo ->
                            mainViewModel.startChatWith(currentUser, uid, name, photo) { convId ->
                                navController.navigate(Routes.chatRoute(convId))
                            }
                        }
                    )
                }

                composable(Routes.MESSAGES) {
                    if (isAdmin) {
                        ConversationsListScreen(
                            currentUser = currentUser,
                            conversationsState = conversationsState,
                            directoryState = directoryState,
                            onBack = { navController.popBackStack() },
                            onOpenConversation = { conv ->
                                mainViewModel.openConversation(conv.id)
                                navController.navigate(Routes.chatRoute(conv.id))
                            },
                            onStartChatWith = { uid, name, photo ->
                                mainViewModel.startChatWith(currentUser, uid, name, photo) { convId ->
                                    navController.navigate(Routes.chatRoute(convId))
                                }
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(Routes.CHAT) { entry ->
                    if (isAdmin) {
                        val convId = entry.arguments?.getString("conversationId").orEmpty()
                        LaunchedEffect(convId) {
                            if (convId.isNotBlank()) mainViewModel.openConversation(convId)
                        }
                        val convList = (conversationsState as? UiState.Success)?.data.orEmpty()
                        val conv = convList.firstOrNull { it.id == convId }

                        ChatScreen(
                            conversationId = convId,
                            currentUser = currentUser,
                            conversation = conv,
                            messagesState = activeMessagesState,
                            orgConfig = orgConfig,
                            onBack = { navController.popBackStack() },
                            onSendMessage = { participants, text, img ->
                                mainViewModel.sendMessage(convId, participants, currentUser.name, text, img)
                            },
                            onMarkSeen = { msgs ->
                                mainViewModel.markMessagesSeen(convId, msgs)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(Routes.NOTIFICATIONS) {
                    if (isAdmin) {
                        NotificationsScreen(
                            currentUid = currentUser.uid,
                            notificationsState = notificationsState,
                            onBack = { navController.popBackStack() },
                            onMarkRead = mainViewModel::markNotificationRead
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(Routes.FINANCE) {
                    if (isAdmin) {
                        FinanceScreen(
                            isAdmin = isAdmin,
                            financialSummary = financialSummary,
                            myContributionsState = myContributionsState,
                            adminContributionsState = adminContributionsState,
                            adminDonationsState = adminDonationsState,
                            adminExpensesState = adminExpensesState,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }

                composable(Routes.ADMIN) {
                    if (isAdmin) {
                        AdminDashboardScreen(
                            currentUser = currentUser,
                            viewModel = mainViewModel,
                            onApproveMemberWithTrade = approveMemberAndSync,
                            onRejectMember = rejectMemberAndSync,
                            onDeleteMemberWithAccount = deleteMemberAndSync,
                            onBack = { navController.popBackStack() },
                            onOpenBloodRequestFlow = { navController.navigate(Routes.BLOOD_DONATION) }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
            }
        }
    }
}
