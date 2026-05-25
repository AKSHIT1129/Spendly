package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.AppNotification
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SpendlyDashboard(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val savingGoals by viewModel.savingGoals.collectAsStateWithLifecycle()
    val billReminders by viewModel.billReminders.collectAsStateWithLifecycle()

    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()
    val inAppNotification by viewModel.notification.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val notificationsList by viewModel.notificationsList.collectAsStateWithLifecycle()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val currencyRate by viewModel.currencyRate.collectAsStateWithLifecycle()

    // Screen display triggers
    var showOnboarding by remember { mutableStateOf(false) } // Set to true to show Welcome page initially!
    var showExchangeScreen by remember { mutableStateOf(false) } // Screen 3
    var activeCategoryIndex by remember { mutableIntStateOf(0) } // Category Slider: 0: Recent Transactions, 1: Active Budgets, 2: Goals Vault, 3: Upcoming Bills
    var activeBottomDockTab by remember { mutableIntStateOf(0) } // Dock: 0: Home, 1: Transactions scroll, 2: Monthly chart analysis, 3: Profile list

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var txDialogTypeIsExpense by remember { mutableStateOf(true) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showDepositGoalDialog by remember { mutableStateOf<SavingGoal?>(null) }
    var showMemberProfileManageDialog by remember { mutableStateOf<Member?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val currentMember = remember(members, selectedMemberId) {
        members.find { it.id == selectedMemberId }
    }

    // Dynamic currency support elements
    val convertedTransactions = remember(transactions, currencyRate) {
        transactions.map { it.copy(amount = it.amount * currencyRate) }
    }
    val convertedBudgets = remember(budgets, currencyRate) {
        budgets.map { it.copy(monthlyLimit = it.monthlyLimit * currencyRate) }
    }
    val convertedSavingGoals = remember(savingGoals, currencyRate) {
        savingGoals.map { it.copy(
            currentAmount = it.currentAmount * currencyRate,
            targetAmount = it.targetAmount * currencyRate
        ) }
    }
    val convertedBillReminders = remember(billReminders, currencyRate) {
        billReminders.map { it.copy(amount = it.amount * currencyRate) }
    }

    // Filter transactions list
    val filteredTransactions = remember(convertedTransactions, selectedMemberId, searchQuery) {
        val baseList = if (selectedMemberId == null) {
            convertedTransactions
        } else {
            convertedTransactions.filter { it.memberId == selectedMemberId }
        }
        if (searchQuery.isEmpty()) {
            baseList
        } else {
            baseList.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredBudgets = remember(convertedBudgets, searchQuery) {
        if (searchQuery.isEmpty()) {
            convertedBudgets
        } else {
            convertedBudgets.filter { it.category.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredSavingGoals = remember(convertedSavingGoals, searchQuery) {
        if (searchQuery.isEmpty()) {
            convertedSavingGoals
        } else {
            convertedSavingGoals.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredBillReminders = remember(convertedBillReminders, searchQuery) {
        if (searchQuery.isEmpty()) {
            convertedBillReminders
        } else {
            convertedBillReminders.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val cosmicBgModifier = Modifier
        .fillMaxSize()
        .drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF221535),
                        Color(0xFF0E0A17),
                        Color(0xFF040306)
                    )
                )
            )
            // Glowing neon ambient back-orbs
            if (size.width > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF5B21B6).copy(alpha = 0.55f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, 0f),
                        radius = size.width * 1.3f
                    ),
                    radius = size.width * 1.3f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, 0f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD946EF).copy(alpha = 0.15f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.8f),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.8f)
                )
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = showOnboarding,
            transitionSpec = {
                (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn()) togetherWith
                        (slideOutHorizontally() + fadeOut())
            },
            label = "applet_onboarding"
        ) { onboardingActive ->
            if (onboardingActive) {
                OnboardingScreen(onGetStarted = { showOnboarding = false })
            } else if (showExchangeScreen) {
                ExchangeScreen(
                    members = members,
                    currencySymbol = currencySymbol,
                    onBack = { showExchangeScreen = false },
                    onExchangeCompleted = { fromId, toId, amt ->
                        val fromName = members.find { it.id == fromId }?.name ?: "Source"
                        val toName = members.find { it.id == toId }?.name ?: "Target"

                        viewModel.addTransaction(-amt, "Transfer", "Transferred to $toName", fromId, false)
                        viewModel.addTransaction(amt, "Transfer", "Received from $fromName", toId, false)
                        viewModel.showInAppNotification("Transferred $currencySymbol${String.format("%,.0f", amt)} successfully!")
                        showExchangeScreen = false
                    }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(76.dp),
                                shape = RoundedCornerShape(26.dp),
                                color = Color(0x4D120E21),
                                border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                                tonalElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloatingDockItem(
                                        icon = Icons.Default.Home,
                                        contentDescription = "Home",
                                        isSelected = activeBottomDockTab == 0,
                                        onSelect = {
                                            activeBottomDockTab = 0
                                            activeCategoryIndex = 0
                                        }
                                    )
                                    FloatingDockItem(
                                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = "Transactions",
                                        isSelected = activeBottomDockTab == 1,
                                        onSelect = {
                                            activeBottomDockTab = 1
                                            activeCategoryIndex = 0
                                        }
                                    )
                                    FloatingDockItem(
                                        icon = Icons.Default.BarChart,
                                        contentDescription = "Charts",
                                        isSelected = activeBottomDockTab == 2,
                                        onSelect = { activeBottomDockTab = 2 }
                                    )
                                    FloatingDockItem(
                                        icon = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        isSelected = activeBottomDockTab == 3,
                                        onSelect = { activeBottomDockTab = 3 }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = cosmicBgModifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = activeBottomDockTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "dock_tab_routing"
                        ) { dockTab ->
                            when (dockTab) {
                                1 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    ) {
                                        Text("Wallet Transactions", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(14.dp))
                                        LedgerListSubScreen(
                                            transactions = filteredTransactions,
                                            members = members,
                                            onDeleteClick = { viewModel.deleteTransaction(it) },
                                            currencySymbol = currencySymbol
                                        )
                                    }
                                }
                                2 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    ) {
                                        Text("Financial Growth", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(14.dp))
                                        AnalyticsScreen(
                                            transactions = filteredTransactions,
                                            members = members,
                                            budgets = convertedBudgets,
                                            currencySymbol = currencySymbol
                                        )
                                    }
                                }
                                3 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    ) {
                                        Text("Profiles & Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            item {
                                                MemberAvatarItem(
                                                    displayName = "All Vaults",
                                                    initials = "ALL",
                                                    signatureColor = Color(0xFFE2F163),
                                                    isSelected = selectedMemberId == null,
                                                    onSelect = { viewModel.selectMember(null) },
                                                    onManage = {},
                                                    isAllShared = true
                                                )
                                            }
                                            items(members) { item ->
                                                val parsedColor = parseHexColor(item.colorHex)
                                                val initials = item.name.trim().split(" ")
                                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                    .take(2)
                                                    .joinToString("")
                                                MemberAvatarItem(
                                                    displayName = item.name,
                                                    initials = initials.ifEmpty { "U" },
                                                    signatureColor = parsedColor,
                                                    isSelected = selectedMemberId == item.id,
                                                    onSelect = { viewModel.selectMember(item.id) },
                                                    onManage = { showMemberProfileManageDialog = item }
                                                )
                                            }
                                            item {
                                                IconButton(
                                                    onClick = { showAddMemberDialog = true },
                                                    modifier = Modifier
                                                        .size(62.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0x33FFFFFF))
                                                ) {
                                                    Icon(Icons.Default.Add, "Add Member", tint = Color.White)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text("Bill Reminders", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        BillsScreen(
                                            billReminders = convertedBillReminders,
                                            onAddBillClick = { showAddBillDialog = true },
                                            onTogglePaid = { viewModel.toggleBillPaid(it) },
                                            onSimulateAlert = { viewModel.simulateBillNotification(it) },
                                            onDeleteBill = { viewModel.deleteBillReminder(it) },
                                            currencySymbol = currencySymbol
                                        )
                                    }
                                }
                                else -> {
                                    // Dock Tab 0: Core mockup screen 2
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                val greetName = currentMember?.name ?: "Akshit"
                                                Text(
                                                    text = "Hello, $greetName",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    letterSpacing = (-0.5).sp
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Text(
                                                        text = "Welcome to Spendly Space",
                                                        fontSize = 13.sp,
                                                        color = Color(0xFFA1A1AA)
                                                    )
                                                    Text(
                                                        text = "•",
                                                        fontSize = 12.sp,
                                                        color = Color(0x4DFFFFFF)
                                                    )
                                                    Text(
                                                        text = "Built by Akshit",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFFE2F163)
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { isSearchActive = !isSearchActive },
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0x2BFFFFFF))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "Search",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Box {
                                                    IconButton(
                                                        onClick = {
                                                            showNotificationsDialog = true
                                                        },
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0x2BFFFFFF))
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Notifications,
                                                            contentDescription = "Alerts",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(if (notificationsList.isNotEmpty()) Color(0xFFE2F163) else Color.Transparent)
                                                            .align(Alignment.TopEnd)
                                                            .offset(x = (-2).dp, y = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        AnimatedVisibility(
                                            visible = isSearchActive,
                                            enter = slideInVertically() + fadeIn(),
                                            exit = slideOutVertically() + fadeOut()
                                        ) {
                                            OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                placeholder = { Text("Filter logs...", color = Color.Gray, fontSize = 13.sp) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                                    .height(52.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0x3B1F222C),
                                                    unfocusedContainerColor = Color(0x3B1F222C),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = Color(0xFFE2F163),
                                                    unfocusedBorderColor = Color(0x33FFFFFF)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))

                                        // Total aggregated metrics card
                                        val totalSum = filteredTransactions.sumOf { it.amount }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                                .clip(RoundedCornerShape(28.dp))
                                                .background(Color(0x1BFFFFFF))
                                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(28.dp))
                                                .padding(horizontal = 24.dp, vertical = 28.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                val cardLabel = if (currentMember == null) "Wallet(USD)" else "Wallet(${currentMember.name.uppercase()})"
                                                Text(
                                                    text = cardLabel,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFA1A1AA),
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = if (totalSum >= 0) {
                                                        "${currencySymbol}${String.format("%,.2f", totalSum)}"
                                                    } else {
                                                        "-${currencySymbol}${String.format("%,.2f", -totalSum)}"
                                                    },
                                                    fontSize = 44.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    letterSpacing = (-1.2).sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                val surplusLabel = if (totalSum >= 0) "+5.03%" else "-2.45%"
                                                Text(
                                                    text = surplusLabel,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (totalSum >= 0) Color(0xFFE2F163) else Color(0xFFF87171),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (totalSum >= 0) Color(0x21E2F163) else Color(0x21F87171))
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Action circle row details
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                DashboardCircleAction(
                                                    icon = Icons.Default.CallMade,
                                                    contentDescription = "Send",
                                                    onClick = {
                                                        txDialogTypeIsExpense = true
                                                        showAddTxDialog = true
                                                    }
                                                )
                                                DashboardCircleAction(
                                                    icon = Icons.Default.SouthWest,
                                                    contentDescription = "Receive",
                                                    onClick = {
                                                        txDialogTypeIsExpense = false
                                                        showAddTxDialog = true
                                                    }
                                                )
                                                DashboardCircleAction(
                                                    icon = Icons.Default.SwapVert,
                                                    contentDescription = "Exchange",
                                                    onClick = { showExchangeScreen = true }
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    txDialogTypeIsExpense = true
                                                    showAddTxDialog = true
                                                },
                                                shape = RoundedCornerShape(24.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color.Black
                                                ),
                                                modifier = Modifier
                                                    .width(82.dp)
                                                    .height(54.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Plus",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(26.dp))

                                        // Category slider capsule layout using horizontally scrollable Row
                                        val categories = listOf("Recent Transactions", "Active Budgets", "Goals Vault", "Upcoming Bills")
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(Color(0x2B100E19))
                                                .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(22.dp))
                                                .padding(4.dp)
                                                .padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            categories.forEachIndexed { idx, label ->
                                                val isSelected = activeCategoryIndex == idx
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .background(if (isSelected) Color(0xFFE2F163) else Color.Transparent)
                                                        .clickable { activeCategoryIndex = idx }
                                                        .padding(horizontal = 16.dp, vertical = 9.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isSelected) Color.Black else Color(0xFFADAEB5),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            AnimatedContent(
                                                targetState = activeCategoryIndex,
                                                transitionSpec = {
                                                    (slideInVertically(initialOffsetY = { 80 }) + fadeIn()) togetherWith
                                                            (slideOutVertically(targetOffsetY = { -80 }) + fadeOut())
                                                },
                                                label = "active_content_transitions"
                                            ) { index ->
                                                when (index) {
                                                    1 -> {
                                                        BudgetsAndGoalsListSubScreen(
                                                            transactions = convertedTransactions,
                                                            budgets = filteredBudgets,
                                                            goals = emptyList(),
                                                            onAddBudgetClick = { showAddBudgetDialog = true },
                                                            onAddGoalClick = {},
                                                            onDepositGoalClick = {},
                                                            onDeleteBudget = { viewModel.deleteBudget(it) },
                                                            onDeleteGoal = {},
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                    2 -> {
                                                        BudgetsAndGoalsListSubScreen(
                                                            transactions = convertedTransactions,
                                                            budgets = emptyList(),
                                                            goals = filteredSavingGoals,
                                                            onAddBudgetClick = {},
                                                            onAddGoalClick = { showAddGoalDialog = true },
                                                            onDepositGoalClick = { showDepositGoalDialog = it },
                                                            onDeleteBudget = {},
                                                            onDeleteGoal = { viewModel.deleteSavingGoal(it) },
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                    3 -> {
                                                        BillsScreen(
                                                            billReminders = filteredBillReminders,
                                                            onAddBillClick = { showAddBillDialog = true },
                                                            onTogglePaid = { viewModel.toggleBillPaid(it) },
                                                            onSimulateAlert = { viewModel.simulateBillNotification(it) },
                                                            onDeleteBill = { viewModel.deleteBillReminder(it) },
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                    else -> {
                                                        LedgerListSubScreen(
                                                            transactions = filteredTransactions,
                                                            members = members,
                                                            onDeleteClick = { viewModel.deleteTransaction(it) },
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = inAppNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(99f)
                .padding(16.dp)
        ) {
            inAppNotification?.let { msg ->
                NotificationToastBanner(msg) { viewModel.dismissNotification() }
            }
        }
    }

    if (showNotificationsDialog) {
        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140E1B)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications & Logs",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showNotificationsDialog = false }) {
                            Icon(Icons.Default.Close, "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (notificationsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active alerts or logs.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notificationsList) { alert ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF)),
                                    border = BorderStroke(1.dp, Color(0x11FFFFFF)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2F163))
                                        )
                                        Text(
                                            text = alert.message,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.clearAllNotifications()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear All", color = Color(0xFFF87171))
                        }
                        Button(
                            onClick = { showNotificationsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2F163)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = { name, role, col ->
                viewModel.addMember(name, col, role)
                showAddMemberDialog = false
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onSave = { category, limit ->
                viewModel.addBudget(category, limit)
                showAddBudgetDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, target, current, date ->
                viewModel.addSavingGoal(title, target, current, date)
                showAddGoalDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    if (showAddTxDialog) {
        AddTransactionDialog(
            members = members,
            onDismiss = { showAddTxDialog = false },
            onSave = { amount, cat, desc, mId, shared, isExp ->
                val adjustedAmount = if (isExp) -kotlin.math.abs(amount) else kotlin.math.abs(amount)
                viewModel.addTransaction(adjustedAmount, cat, desc, mId, shared)
                showAddTxDialog = false
            },
            currencySymbol = currencySymbol,
            initialIsExpense = txDialogTypeIsExpense
        )
    }

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onSave = { title, amount, date, category ->
                viewModel.addBillReminder(title, amount, date, category)
                showAddBillDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    showDepositGoalDialog?.let { goal ->
        DepositGoalDialog(
            goal = goal,
            onDismiss = { showDepositGoalDialog = null },
            onDeposit = { money ->
                viewModel.updateSavingProgress(goal, money)
                showDepositGoalDialog = null
            },
            currencySymbol = currencySymbol
        )
    }

    showMemberProfileManageDialog?.let { member ->
        MemberProfileDetailDialog(
            member = member,
            transactionsCount = transactions.count { it.memberId == member.id },
            onDismiss = { showMemberProfileManageDialog = null },
            onDelete = {
                viewModel.deleteMember(member)
                showMemberProfileManageDialog = null
            }
        )
    }
}

@Composable
fun FloatingDockItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2F163)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFFA1A1AA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DashboardCircleAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0x24FFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161129),
                            Color(0xFF0F0B1C),
                            Color(0xFF08060E)
                        )
                    )
                )
                if (size.width > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x29F43F5E), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            radius = size.width * 0.9f
                        ),
                        radius = size.width * 0.9f,
                        center = androidx.compose.ui.geometry.Offset(size.width, size.height)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x2BD946EF), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(0f, size.height * 0.4f),
                            radius = size.width * 0.8f
                        ),
                        radius = size.width * 0.8f,
                        center = androidx.compose.ui.geometry.Offset(0f, size.height * 0.4f)
                    )
                }
            }
            .safeDrawingPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                contentAlignment = Alignment.Center
            ) {
                FloatingGraphicCanvas()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Unlock the\nFuture of\nFinance",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 50.sp,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Gain full visibility of your transactions, budgets, bills, and user profiles beautifully.",
                    fontSize = 15.sp,
                    color = Color(0xFFA1A1AA),
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(36.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF141318))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(32.dp))
                        .clickable(onClick = onGetStarted)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2F163)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "Get Started",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 36.dp)
                        )
                        Spacer(modifier = Modifier.width(32.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    members: List<Member>,
    currencySymbol: String,
    onBack: () -> Unit,
    onExchangeCompleted: (fromMemberId: Int, toMemberId: Int, amount: Double) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var selectedFromMemberId by remember { mutableIntStateOf(members.firstOrNull()?.id ?: 1) }
    var selectedToMemberId by remember { mutableIntStateOf(members.getOrNull(1)?.id ?: (members.firstOrNull()?.id ?: 1)) }

    var expandedFromMenu by remember { mutableStateOf(false) }
    var expandedToMenu by remember { mutableStateOf(false) }

    val fromMemberName = members.find { it.id == selectedFromMemberId }?.name ?: "Source"
    val toMemberName = members.find { it.id == selectedToMemberId }?.name ?: "Recipient"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF110E1A),
                            Color(0xFF09080E)
                        )
                    )
                )
            }
            .safeDrawingPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x3BFFFFFF))
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = "Exchange",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x3D1F222C)),
                border = BorderStroke(1.dp, Color(0x1F9FC8EB))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Send", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2BFFFFFF))
                                    .clickable { expandedFromMenu = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fromMemberName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedFromMenu,
                                onDismissRequest = { expandedFromMenu = false },
                                modifier = Modifier.background(Color(0xFF22252C))
                            ) {
                                members.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.name, color = Color.White) },
                                        onClick = {
                                            selectedFromMemberId = m.id
                                            expandedFromMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        TextField(
                            value = rawAmount,
                            onValueChange = { rawAmount = it },
                            placeholder = { Text("0.00", color = Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.End
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(160.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Balance: Available",
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = selectedFromMemberId
                        selectedFromMemberId = selectedToMemberId
                        selectedToMemberId = temp
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2F163))
                        .border(4.dp, Color(0xFF09080E), CircleShape)
                ) {
                    Icon(Icons.Default.SwapVert, "Toggle Swap", tint = Color.Black)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x3D1F222C)),
                border = BorderStroke(1.dp, Color(0x1F9FC8EB))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Receive", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2BFFFFFF))
                                    .clickable { expandedToMenu = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(toMemberName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedToMenu,
                                onDismissRequest = { expandedToMenu = false },
                                modifier = Modifier.background(Color(0xFF22252C))
                            ) {
                                members.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.name, color = Color.White) },
                                        onClick = {
                                            selectedToMemberId = m.id
                                            expandedToMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (rawAmount.isEmpty()) "0.00" else rawAmount,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instantly credited to wallet",
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    val amountVal = rawAmount.toDoubleOrNull() ?: 0.0
                    if (amountVal > 0 && selectedFromMemberId != selectedToMemberId) {
                        onExchangeCompleted(selectedFromMemberId, selectedToMemberId, amountVal)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = rawAmount.isNotEmpty() && selectedFromMemberId != selectedToMemberId
            ) {
                Text("Exchange", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Rate", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                        Text("1 $currencySymbol = 1.00 $currencySymbol", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Price impact", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                        Text("0.05%", color = Color(0xFFE2F163), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Liquidity Provider Fee", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                        Text("Free Sync Transfer", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingGraphicCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width > 0f && height > 0f) {
            val btcX = width * 0.28f
            val btcY = height * 0.45f + bounceY
            val btcRadius = width * 0.24f

            val btcRadialRadius = btcRadius * 1.6f
            if (btcRadialRadius > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE2F163).copy(alpha = 0.22f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(btcX, btcY),
                        radius = btcRadialRadius
                    ),
                    radius = btcRadialRadius,
                    center = androidx.compose.ui.geometry.Offset(btcX, btcY)
                )
            }

            if (btcRadius > 0f) {
                drawCircle(
                    color = Color(0xFF191823),
                    radius = btcRadius,
                    center = androidx.compose.ui.geometry.Offset(btcX, btcY)
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE2F163), Color(0xFF10B981)),
                        start = androidx.compose.ui.geometry.Offset(btcX - btcRadius, btcY - btcRadius),
                        end = androidx.compose.ui.geometry.Offset(btcX + btcRadius, btcY + btcRadius)
                    ),
                    radius = btcRadius,
                    center = androidx.compose.ui.geometry.Offset(btcX, btcY),
                    style = Stroke(width = 6f)
                )

                drawCircle(
                    color = Color(0xFFE2F163).copy(alpha = 0.8f),
                    radius = btcRadius * 0.45f,
                    center = androidx.compose.ui.geometry.Offset(btcX, btcY),
                    style = Stroke(width = 5f)
                )
            }

            val solX = width * 0.72f
            val solY = height * 0.58f - bounceY
            val solRadius = width * 0.20f

            val solRadialRadius = solRadius * 1.5f
            if (solRadialRadius > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC06240).copy(alpha = 0.18f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(solX, solY),
                        radius = solRadialRadius
                    ),
                    radius = solRadialRadius,
                    center = androidx.compose.ui.geometry.Offset(solX, solY)
                )
            }

            if (solRadius > 0f) {
                drawCircle(
                    color = Color(0xFF14131A),
                    radius = solRadius,
                    center = androidx.compose.ui.geometry.Offset(solX, solY)
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFC06240), Color(0xFF8B5CF6)),
                        start = androidx.compose.ui.geometry.Offset(solX - solRadius, solY - solRadius),
                        end = androidx.compose.ui.geometry.Offset(solX + solRadius, solY + solRadius)
                    ),
                    radius = solRadius,
                    center = androidx.compose.ui.geometry.Offset(solX, solY),
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LedgerListSubScreen(
    transactions: List<Transaction>,
    members: List<Member>,
    onDeleteClick: (Transaction) -> Unit,
    currencySymbol: String = "$"
) {
    if (transactions.isEmpty()) {
        CardEmptyPlaceholder(
            title = "Awaiting financial stats",
            subtitle = "Settle custom revenue or spend entries to start real-time analytics aggregation."
        )
        return
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transactions) { tx ->
            val isExpense = tx.amount < 0
            val author = members.find { it.id == tx.memberId }?.name ?: "Guest"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(20.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { onDeleteClick(tx) }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isExpense) Color(0x2BFF5252) else Color(0x2B10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(tx.category),
                            contentDescription = tx.category,
                            tint = if (isExpense) Color(0xFFF87171) else Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.widthIn(max = 160.dp)) {
                        Text(
                            text = tx.description.ifEmpty { tx.category },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "by $author • ${tx.category}",
                            color = Color(0xFFA1A1AA),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isExpense) {
                            "-${currencySymbol}${String.format("%,.2f", -tx.amount)}"
                        } else {
                            "+${currencySymbol}${String.format("%,.2f", tx.amount)}"
                        },
                        color = if (isExpense) Color(0xFFF87171) else Color(0xFF34D399),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatter.format(Date(tx.date)),
                        color = Color(0x7FFFFFFF),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetsAndGoalsListSubScreen(
    transactions: List<Transaction>,
    budgets: List<Budget>,
    goals: List<SavingGoal>,
    onAddBudgetClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onDepositGoalClick: (SavingGoal) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
    onDeleteGoal: (SavingGoal) -> Unit,
    currencySymbol: String = "$"
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (budgets.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Category Limit Thresholds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    IconButton(onClick = onAddBudgetClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.AddCircle, null, tint = Color(0xFFE2F163))
                    }
                }
            }

            items(budgets) { budget ->
                val spent = transactions
                    .filter { it.category.equals(budget.category, ignoreCase = true) && it.amount < 0 }
                    .sumOf { -it.amount }
                val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
                val color = if (ratio > 0.85f) Color(0xFFF87171) else Color(0xFFE2F163)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = {}, onLongClick = { onDeleteBudget(budget) }),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x19FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(budget.category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${currencySymbol}${String.format("%.0f", spent)} / ${currencySymbol}${String.format("%.0f", budget.monthlyLimit)}", color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = ratio,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = color,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }
            }
        }

        if (goals.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Savings Vault Target", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    IconButton(onClick = onAddGoalClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.AddCircle, null, tint = Color(0xFFE2F163))
                    }
                }
            }

            items(goals) { goal ->
                val ratio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onDepositGoalClick(goal) },
                            onLongClick = { onDeleteGoal(goal) }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x19FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(goal.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${currencySymbol}${String.format("%.0f", goal.currentAmount)} / ${currencySymbol}${String.format("%.0f", goal.targetAmount)}", color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = ratio,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = Color(0xFF10B981),
                            trackColor = Color(0x33FFFFFF)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Target Date: ${goal.targetDate} (Tap to deposit cash)", color = Color(0xFFA1A1AA), fontSize = 11.sp)
                    }
                }
            }
        }

        if (budgets.isEmpty() && goals.isEmpty()) {
            item {
                CardEmptyPlaceholder("No Limits Configured", "Record dynamic category limits or savings goals in profile vault.")
            }
        }
    }
}

@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    members: List<Member>,
    budgets: List<Budget>,
    currencySymbol: String = "$"
) {
    if (transactions.isEmpty()) {
        CardEmptyPlaceholder(
            title = "Awaiting financial stats",
            subtitle = "Settle custom revenue or spend entries to start real-time analytics aggregation."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val income = transactions.filter { it.amount > 0 }.sumOf { it.amount }
            val expense = transactions.filter { it.amount < 0 }.sumOf { -it.amount }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x19FFFFFF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x2110B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = "Income",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sourced", fontSize = 11.sp, color = Color(0xFFA1A1AA), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${currencySymbol}${String.format("%,.0f", income)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x19FFFFFF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x21FF5252)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = "Expense",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Expended", fontSize = 11.sp, color = Color(0xFFA1A1AA), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${currencySymbol}${String.format("%,.0f", expense)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                border = BorderStroke(1.dp, Color(0x19FFFFFF)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Spend Categorically Breakdown", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    val spendsByCategory = transactions
                        .filter { it.amount < 0 }
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { -it.amount } }

                    if (spendsByCategory.isEmpty()) {
                        Text("No expenses logged details", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                    } else {
                        val totalSpend = spendsByCategory.values.sum()
                        spendsByCategory.forEach { (cat, amt) ->
                            val percent = if (totalSpend > 0) (amt / totalSpend).toFloat() else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE2F163)))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat, color = Color.White, fontSize = 12.sp)
                                }
                                Text("${currencySymbol}${String.format("%.0f", amt)} (${String.format("%.0f", percent * 100)}%)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerScreen(
    transactions: List<Transaction>,
    members: List<Member>,
    onAddClick: () -> Unit,
    onDeleteClick: (Transaction) -> Unit,
    currencySymbol: String = "$"
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LedgerListSubScreen(
            transactions = transactions,
            members = members,
            onDeleteClick = onDeleteClick,
            currencySymbol = currencySymbol
        )

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 16.dp)
                .testTag("add_transaction_fab")
        ) {
            Icon(Icons.Default.Add, "Add Transaction")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillsScreen(
    billReminders: List<BillReminder>,
    onAddBillClick: () -> Unit,
    onTogglePaid: (BillReminder) -> Unit,
    onSimulateAlert: (BillReminder) -> Unit,
    onDeleteBill: (BillReminder) -> Unit,
    currencySymbol: String = "$"
) {
    if (billReminders.isEmpty()) {
        CardEmptyPlaceholder(
            title = "All utilities clear",
            subtitle = "Set up structured alert reminders on electricity, internet, or credit lines easily."
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(
                onClick = onAddBillClick,
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 120.dp, end = 16.dp)
                    .testTag("add_bill_empty_fab")
            ) {
                Icon(Icons.Default.Add, "add_btn")
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(billReminders) { bill ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(20.dp))
                        .combinedClickable(onClick = {}, onLongClick = { onDeleteBill(bill) })
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = bill.isPaid,
                            onCheckedChange = { onTogglePaid(bill) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE2F163), uncheckedColor = Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = bill.title,
                                color = if (bill.isPaid) Color.Gray else Color.White,
                                textDecoration = if (bill.isPaid) TextDecoration.LineThrough else null,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text("Due in ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(bill.dueDate))}", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${currencySymbol}${String.format("%.0f", bill.amount)}", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        IconButton(onClick = { onSimulateAlert(bill) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.NotificationsActive, "Ping alert", tint = Color(0xFFE2F163), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddBillClick,
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp)
                .testTag("add_bill_fab")
        ) {
            Icon(Icons.Default.Add, "New Alert")
        }
    }
}

@Composable
fun CardEmptyPlaceholder(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x13FFFFFF)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0x19FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = Color(0xFFE2F163), modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
    }
}

@Composable
fun NotificationToastBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1D2B)),
        border = BorderStroke(1.dp, Color(0xFFE2F163).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Info, null, tint = Color(0xFFE2F163))
                Spacer(modifier = Modifier.width(10.dp))
                Text(message, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Family member") }
    var colHex by remember { mutableStateOf("#10B981") }

    val activeColors = listOf("#10B981", "#6366F1", "#F59E0B", "#EF4444", "#EC4899", "#8B5CF6")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222F)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Provision New Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Assigned Role", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Admin", "Member", "Child").forEach { r ->
                        val active = r == role
                        Button(
                            onClick = { role = r },
                            colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFE2F163) else Color(0x1AFFFFFF), contentColor = if (active) Color.Black else Color.White)
                        ) {
                            Text(r, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("Vault Color", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeColors.forEach { color ->
                        val active = color == colHex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(if (active) 2.dp else 0.dp, Color.White, CircleShape)
                                .clickable { colHex = color }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { if (name.isNotEmpty()) onSave(name, role, colHex) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Profile")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onSave: (Double, String, String, Int, Boolean, Boolean) -> Unit,
    currencySymbol: String = "$",
    initialIsExpense: Boolean = true
) {
    var isExpense by remember { mutableStateOf(initialIsExpense) }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var description by remember { mutableStateOf("") }
    var chosenMemberId by remember { mutableIntStateOf(members.firstOrNull()?.id ?: 1) }
    var isShared by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Rent", "Salary", "Shopping", "Entertainment", "Utilities", "Investment", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222F)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Record Transaction", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0x33FFFFFF))) {
                    Button(
                        onClick = { isExpense = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isExpense) Color(0xFFF87171) else Color.Transparent)
                    ) {
                        Text("Expense", color = if (isExpense) Color.White else Color.Gray)
                    }
                    Button(
                        onClick = { isExpense = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isExpense) Color(0xFF10B981) else Color.Transparent)
                    ) {
                        Text("Income", color = if (!isExpense) Color.White else Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currencySymbol)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Category", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        val active = category == cat
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFE2F163) else Color(0x1F22252F), contentColor = if (active) Color.Black else Color.White)
                        ) {
                            Text(cat, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("Assigned Member", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    members.forEach { m ->
                        val active = chosenMemberId == m.id
                        Button(
                            onClick = { chosenMemberId = m.id },
                            colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFF6366F1) else Color(0x1F22252F), contentColor = Color.White)
                        ) {
                            Text(m.name, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val value = amountStr.toDoubleOrNull() ?: 0.0
                        if (value > 0) onSave(value, category, description, chosenMemberId, isShared, isExpense)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Record Transaction")
                }
            }
        }
    }
}

@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit, currencySymbol: String = "$") {
    var category by remember { mutableStateOf("Food") }
    var limitStr by remember { mutableStateOf("") }
    val categories = listOf("Food", "Rent", "Salary", "Shopping", "Entertainment", "Utilities", "Investment", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222F)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Set Threshold Limits", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Budget Limit ($currencySymbol)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Select Category", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        val active = category == cat
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFE2F163) else Color(0x1F22252F), contentColor = if (active) Color.Black else Color.White)
                        ) {
                            Text(cat, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val value = limitStr.toDoubleOrNull() ?: 0.0
                        if (value > 0) onSave(category, value)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save threshold")
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onSave: (String, Double, Double, String) -> Unit, currencySymbol: String = "$") {
    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var currentStr by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("Dec 2026") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222F)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("New Savings Vault Goal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target Amount ($currencySymbol)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = currentStr,
                    onValueChange = { currentStr = it },
                    label = { Text("Current Safe Amount ($currencySymbol)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Date", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val target = targetStr.toDoubleOrNull() ?: 0.0
                        val current = currentStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotEmpty() && target > 0) onSave(title, target, current, targetDate)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Goal")
                }
            }
        }
    }
}

@Composable
fun AddBillDialog(onDismiss: () -> Unit, onSave: (String, Double, Long, String) -> Unit, currencySymbol: String = "$") {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Utilities") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222F)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("New Utility Alert", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Issuer Name", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Bill Amount ($currencySymbol)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val value = amountStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotEmpty() && value > 0) {
                            onSave(title, value, System.currentTimeMillis() + 86400000 * 5, category)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Utility Reminder")
                }
            }
        }
    }
}

@Composable
fun DepositGoalDialog(goal: SavingGoal, onDismiss: () -> Unit, onDeposit: (Double) -> Unit, currencySymbol: String = "$") {
    var amountStr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222F)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Deposit Cash to ${goal.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Deposit cash ($currencySymbol)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFE2F163)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val x = amountStr.toDoubleOrNull() ?: 0.0
                        if (x > 0) onDeposit(x)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Deposit")
                }
            }
        }
    }
}

@Composable
fun MemberProfileDetailDialog(member: Member, transactionsCount: Int, onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F222F)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text(member.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Role: ${member.role}", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Total transactions recorded: $transactionsCount", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F22252F), contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171), contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete Profile")
                    }
                }
            }
        }
    }
}

@Composable
fun MemberAvatarItem(
    displayName: String,
    initials: String,
    signatureColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onManage: () -> Unit,
    isAllShared: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(62.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFE2F163).copy(alpha = 0.12f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFFE2F163) else Color(0x33FFFFFF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isAllShared) Color(0x38FFFFFF) else signatureColor.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAllShared) {
                        Icon(
                            imageVector = Icons.Default.AllInclusive,
                            contentDescription = null,
                            tint = Color(0xFFE2F163),
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = initials,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = signatureColor,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            if (!isAllShared) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 2.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22252D))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { onManage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Manage",
                        tint = signatureColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayName,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 64.dp)
        )
    }
}

fun getCategoryIcon(cat: String): ImageVector {
    return when (cat.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "rent" -> Icons.Default.Home
        "salary" -> Icons.Default.AccountBalanceWallet
        "shopping" -> Icons.Default.ShoppingBag
        "entertainment" -> Icons.Default.Tv
        "utilities" -> Icons.Default.ElectricalServices
        "investment" -> Icons.Default.ShowChart
        "transfer" -> Icons.Default.SwapHoriz
        else -> Icons.Default.Category
    }
}

fun parseHexColor(hex: String, default: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}
