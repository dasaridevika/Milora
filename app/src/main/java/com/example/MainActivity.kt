package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.viewmodel.MilkViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.ui.draw.scale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load dynamic font and color settings
        AppSettingsState.load(this)
        AuthSession.init(this)

        // Setup Room Database, Repository and ViewModel Factory
        val database = AppDatabase.getDatabase(this)
        val repository = MilkRepository(database.milkDao())
        val factory = MilkViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[MilkViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavigation(viewModel)
                    }
                }
            }
        }
    }
}

// Global ViewModel Factory Implementation
class MilkViewModelFactory(private val repository: MilkRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MilkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MilkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AppNavigation(viewModel: MilkViewModel) {
    val context = LocalContext.current
    val user = AuthSession.currentUser
    val role = AuthSession.userRole
    val joinedCode = AuthSession.joinedOwnerCode
    val customerId = AuthSession.currentCustomerId

    LaunchedEffect(user, role, joinedCode, customerId) {
        if (user != null) {
            when (role) {
                UserRole.OWNER -> {
                    viewModel.activeRole.value = "OWNER"
                }
                UserRole.CUSTOMER -> {
                    if (joinedCode.isNotEmpty() && customerId != -1) {
                        viewModel.activeCustomerId.value = customerId
                        viewModel.activeRole.value = "CUSTOMER"
                    } else {
                        viewModel.activeRole.value = "JOIN_CODE"
                    }
                }
                UserRole.NONE -> {
                    viewModel.activeRole.value = "SELECTION"
                }
            }
        } else {
            viewModel.activeRole.value = "SIGN_IN"
        }
    }

    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = activeRole,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "RoleNavigation"
    ) { currentActive ->
        when (currentActive) {
            "SIGN_IN" -> {
                GoogleSignInScreen(
                    onSignInSuccess = { email, name ->
                        AuthSession.loginWithGoogle(context, email, name)
                    }
                )
            }
            "SELECTION" -> {
                RoleSelectionScreen(viewModel)
            }
            "JOIN_CODE" -> {
                CustomerCodeJoinScreen(
                    viewModel = viewModel,
                    onJoined = {
                        viewModel.activeCustomerId.value = AuthSession.currentCustomerId
                        viewModel.activeRole.value = "CUSTOMER"
                    }
                )
            }
            "OWNER" -> {
                OwnerDashboardScreen(viewModel)
            }
            "CUSTOMER" -> {
                CustomerDashboardScreen(viewModel)
            }
        }
    }
}

// --- SCREEN 1: Splash & Role Selection ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(viewModel: MilkViewModel) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    var showCustomerSelect by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsDialog(onDismiss = { showSettings = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Organic background pastoral art using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Warm morning sun glow
            drawCircle(
                color = Color(0xFFFFFDE7),
                radius = canvasWidth * 0.45f,
                center = Offset(canvasWidth * 0.5f, 0f)
            )

            // Dynamic grass pasture curves at the bottom
            drawCircle(
                color = Color(0xFFE8F5E9),
                radius = canvasWidth * 1.5f,
                center = Offset(canvasWidth * 0.5f, canvasHeight + 100f)
            )

            drawCircle(
                color = Color(0xFFC8E6C9),
                radius = canvasWidth * 1.0f,
                center = Offset(canvasWidth * 0.1f, canvasHeight + 200f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // Beautiful exact custom Milora Logo
                MiloraLogo(
                    size = 180.dp,
                    elevation = 6.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Daily Milk Delivery Companion",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )

                // Google Account Details Header
                AuthSession.currentUser?.let { user ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = user.email,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign Out",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clickable {
                                        AuthSession.logout(context)
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            // Central Role Selection Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Your Account Role",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // OWNER CARD
                Card(
                    onClick = { AuthSession.selectRole(context, UserRole.OWNER) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .testTag("role_owner_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Owner icon",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Milk Vendor / Owner",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Manage deliveries, customers, and calculate monthly bills",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // CUSTOMER CARD
                Card(
                    onClick = { AuthSession.selectRole(context, UserRole.CUSTOMER) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .testTag("role_customer_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Customer icon",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Village Customer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pause delivery, request quantity change, track monthly bills",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Footer info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "A trusting village initiative for daily fresh milk.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Customer Selection dialog to simulate specific logins
        if (showCustomerSelect) {
            AlertDialog(
                onDismissRequest = { showCustomerSelect = false },
                title = { Text("Select Customer Profile") },
                text = {
                    Column {
                        Text(
                            text = "To simulate a customer dashboard, select one of the registered village profiles:",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (customers.isEmpty()) {
                            Text(
                                text = "Loading profiles... please wait.",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(customers) { c ->
                                    Card(
                                        onClick = {
                                            viewModel.activeCustomerId.value = c.id
                                            viewModel.activeRole.value = "CUSTOMER"
                                            showCustomerSelect = false
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = c.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = "${c.defaultQuantity}L/day • Rs.${c.pricePerLiter}/L",
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Select"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCustomerSelect = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Floating Settings Icon in top-right corner of splash/selection screen
        IconButton(
            onClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Customize Fonts and Colors",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


// --- SCREEN 2: OWNER DASHBOARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(viewModel: MilkViewModel) {
    val ownerTab by viewModel.ownerTab.collectAsStateWithLifecycle()
    val globalConfig by viewModel.globalConfig.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val deliveries by viewModel.dailyDeliveries.collectAsStateWithLifecycle()
    val bills by viewModel.monthlyBills.collectAsStateWithLifecycle()
    val pendingReqs by viewModel.pendingRequests.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val simulatedDate by viewModel.simulatedDate.collectAsStateWithLifecycle()
    val simulatedMonth by viewModel.simulatedMonth.collectAsStateWithLifecycle()

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Milora (Vendor)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    var showSettings by remember { mutableStateOf(false) }
                    if (showSettings) {
                        SettingsDialog(onDismiss = { showSettings = false })
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Customize Fonts and Colors",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showBroadcastDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Broadcast",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    val context = LocalContext.current
                    TextButton(onClick = { AuthSession.logout(context) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logout")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = ownerTab == "DELIVERIES",
                    onClick = { viewModel.ownerTab.value = "DELIVERIES" },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Deliveries") },
                    label = { Text("Deliveries") }
                )
                NavigationBarItem(
                    selected = ownerTab == "CUSTOMERS",
                    onClick = { viewModel.ownerTab.value = "CUSTOMERS" },
                    icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                    label = { Text("Customers") }
                )
                NavigationBarItem(
                    selected = ownerTab == "BILLS",
                    onClick = { viewModel.ownerTab.value = "BILLS" },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Bills") },
                    label = { Text("Bills") }
                )
                NavigationBarItem(
                    selected = ownerTab == "REQUESTS",
                    onClick = { viewModel.ownerTab.value = "REQUESTS" },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingReqs.isNotEmpty()) {
                                    Badge { Text(pendingReqs.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Requests")
                        }
                    },
                    label = { Text("Requests") }
                )
                NavigationBarItem(
                    selected = ownerTab == "NOTIFICATIONS",
                    onClick = { viewModel.ownerTab.value = "NOTIFICATIONS" },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Alerts") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Today's Status Banner and Live Stats Summary
                OwnerDashboardHeader(viewModel)

                // Sub-screen rendering based on selected Tab
                Box(modifier = Modifier.weight(1f)) {
                    when (ownerTab) {
                        "DELIVERIES" -> OwnerDeliveriesTab(viewModel)
                        "CUSTOMERS" -> OwnerCustomersTab(viewModel)
                        "BILLS" -> OwnerBillsTab(viewModel)
                        "REQUESTS" -> OwnerRequestsTab(viewModel)
                        "NOTIFICATIONS" -> OwnerNotificationsTab(viewModel)
                    }
                }
            }
        }

        // Broadcast Announcement Dialog
        if (showBroadcastDialog) {
            AlertDialog(
                onDismissRequest = { showBroadcastDialog = false },
                title = { Text("Broadcast Message to All Customers") },
                text = {
                    Column {
                        Text(
                            text = "Send an emergency notice or greeting (e.g., 'Delivery delayed by 1 hour due to rain'). It will show on all customer notice boards.",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = broadcastText,
                            onValueChange = { broadcastText = it },
                            placeholder = { Text("Write your message here...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (broadcastText.trim().isNotEmpty()) {
                                viewModel.broadcastAnnouncement(broadcastText)
                                broadcastText = ""
                                showBroadcastDialog = false
                            }
                        }
                    ) {
                        Text("Broadcast")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBroadcastDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun OwnerDashboardHeader(viewModel: MilkViewModel) {
    val config by viewModel.globalConfig.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val deliveries by viewModel.dailyDeliveries.collectAsStateWithLifecycle()
    val date by viewModel.simulatedDate.collectAsStateWithLifecycle()

    // Calculate today's sales
    val todayDeliveries = deliveries.filter { it.dateStr == date }
    val totalLitersToday = todayDeliveries.sumOf { it.deliveredQuantity }
    val totalActiveCustomers = customers.size
    val totalLitersSoldMonth = deliveries.filter { it.dateStr.startsWith(date.take(7)) }.sumOf { it.deliveredQuantity }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Row 1: Date & Global Status Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today's Delivery Status",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Global Pause Switch
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (config?.isOwnerPausedToday == true) MaterialTheme.colorScheme.errorContainer else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (config?.isOwnerPausedToday == true) "No Milk Today" else "Delivering Today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (config?.isOwnerPausedToday == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = config?.isOwnerPausedToday != true,
                        onCheckedChange = { isDelivering ->
                            viewModel.toggleOwnerGlobalPause(!isDelivering, "Owner busy / holiday today.")
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.scale(0.75f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Live Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "Liters Today",
                value = "${DecimalFormat("#.##").format(totalLitersToday)}L",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Customers",
                value = "$totalActiveCustomers Active",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Month Vol",
                value = "${DecimalFormat("#.##").format(totalLitersSoldMonth)}L",
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Google Owner Invite Code & Share
        val context = LocalContext.current
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Your Shareable Owner Code",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (AuthSession.ownerCode.isNotEmpty()) AuthSession.ownerCode else "PM-OWNER-77",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = {
                        val code = if (AuthSession.ownerCode.isNotEmpty()) AuthSession.ownerCode else "PM-OWNER-77"
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                "Hi! Join me as a customer on Milora (Daily Milk Delivery App) using my Owner Code: $code\n\nLink your account securely today!"
                            )
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Owner Code")
                        context.startActivity(shareIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

// --- OWNER SUB-SCREENS ---

// 1. Deliveries Checklist Screen
@Composable
fun OwnerDeliveriesTab(viewModel: MilkViewModel) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val deliveries by viewModel.dailyDeliveries.collectAsStateWithLifecycle()
    val date by viewModel.simulatedDate.collectAsStateWithLifecycle()
    val config by viewModel.globalConfig.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Daily Deliveries Checklist",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (customers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.People,
                title = "No Customers Registered",
                desc = "Add customers in the Customers tab to start daily delivery tracking."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customers) { customer ->
                    // Find delivery status for today
                    val deliveryRecord = deliveries.find { it.customerId == customer.id && it.dateStr == date }
                    val currentStatus = deliveryRecord?.deliveryStatus ?: "NOT_DELIVERED"
                    val currentQty = deliveryRecord?.deliveredQuantity ?: customer.defaultQuantity

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = customer.address,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${DecimalFormat("#.##").format(currentQty)} Liters (${customer.defaultQuantity}L default)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Interactive Status Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                when (currentStatus) {
                                    "DELIVERED" -> {
                                        BadgeButton(
                                            label = "Delivered",
                                            icon = Icons.Default.Check,
                                            containerColor = Color(0xFFE8F5E9),
                                            contentColor = Color(0xFF2E7D32),
                                            onClick = { viewModel.setDeliveryStatusForToday(customer.id, "NOT_DELIVERED") }
                                        )
                                    }
                                    "NOT_DELIVERED" -> {
                                        BadgeButton(
                                            label = "Pending",
                                            icon = Icons.Default.Cancel,
                                            containerColor = Color(0xFFECEFF1),
                                            contentColor = Color(0xFF546E7A),
                                            onClick = { viewModel.setDeliveryStatusForToday(customer.id, "DELIVERED") }
                                        )
                                    }
                                    "PAUSED_BY_CUSTOMER" -> {
                                        BadgeButton(
                                            label = "Cust Paused",
                                            icon = Icons.Default.Pause,
                                            containerColor = Color(0xFFFFF3E0),
                                            contentColor = Color(0xFFE65100),
                                            onClick = {
                                                // Let owner bypass customer pause if needed
                                                viewModel.setDeliveryStatusForToday(customer.id, "DELIVERED")
                                            }
                                        )
                                    }
                                    "PAUSED_BY_OWNER" -> {
                                        BadgeButton(
                                            label = "Owner Paused",
                                            icon = Icons.Default.Pause,
                                            containerColor = Color(0xFFFFEBEE),
                                            contentColor = Color(0xFFC62828),
                                            onClick = {
                                                // Resume for just this customer
                                                viewModel.setDeliveryStatusForToday(customer.id, "DELIVERED")
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

@Composable
fun BadgeButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .clickable { onClick() }
            .shadow(1.dp, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

// 2. Customers List Tab (Manage and Add)
@Composable
fun OwnerCustomersTab(viewModel: MilkViewModel) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    // Dialog Input states
    var nameText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }
    var defaultQtyText by remember { mutableStateOf("1.0") }
    var priceText by remember { mutableStateOf("60.0") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    nameText = ""
                    phoneText = ""
                    addressText = ""
                    defaultQtyText = "1.0"
                    priceText = "60.0"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Registered Customers",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (customers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = "No Customers Yet",
                    desc = "Click the '+' button to register your first local milk customer."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers) { c ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = c.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Phone: ${c.phone}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "Locality: ${c.address}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("${c.defaultQuantity}L Daily") }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Rs.${c.pricePerLiter}/L") }
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        editingCustomer = c
                                        nameText = c.name
                                        phoneText = c.phone
                                        addressText = c.address
                                        defaultQtyText = c.defaultQuantity.toString()
                                        priceText = c.pricePerLiter.toString()
                                        showEditDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Customer",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Customer Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Register New Customer") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Customer Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phoneText,
                            onValueChange = { phoneText = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = addressText,
                            onValueChange = { addressText = it },
                            label = { Text("Village Locality / Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = defaultQtyText,
                            onValueChange = { defaultQtyText = it },
                            label = { Text("Default Supply Quantity (Liters)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price Per Liter (Rs.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val qty = defaultQtyText.toDoubleOrNull() ?: 1.0
                            val prc = priceText.toDoubleOrNull() ?: 60.0
                            if (nameText.isNotBlank() && phoneText.isNotBlank()) {
                                viewModel.addNewCustomer(nameText, phoneText, addressText, qty, prc)
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Customer Dialog
        if (showEditDialog && editingCustomer != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Customer Profile") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Customer Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phoneText,
                            onValueChange = { phoneText = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = addressText,
                            onValueChange = { addressText = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = defaultQtyText,
                            onValueChange = { defaultQtyText = it },
                            label = { Text("Default Supply Quantity (L) ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price Per Liter (Rs.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val qty = defaultQtyText.toDoubleOrNull() ?: 1.0
                            val prc = priceText.toDoubleOrNull() ?: 60.0
                            if (nameText.isNotBlank()) {
                                viewModel.editCustomer(editingCustomer!!.id, nameText, phoneText, addressText, qty, prc)
                                showEditDialog = false
                            }
                        }
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// 3. Billing Management Tab
@Composable
fun OwnerBillsTab(viewModel: MilkViewModel) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val bills by viewModel.monthlyBills.collectAsStateWithLifecycle()
    val simulatedMonth by viewModel.simulatedMonth.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Bill Calculation",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { viewModel.forceRecalculateAll() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Recalculate All", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (customers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Receipt,
                title = "No Billings",
                desc = "Registered customer billing reports will appear here automatically."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(customers) { customer ->
                    // Find existing bill for current month
                    val currentBill = bills.find { it.customerId == customer.id && it.monthYearStr == simulatedMonth }
                    val totalLiters = currentBill?.totalLiters ?: 0.0
                    val totalAmount = currentBill?.totalAmount ?: 0.0
                    val paymentStatus = currentBill?.status ?: "UNPAID"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Month Cycle: $simulatedMonth",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                // Status Badge
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusColors = getPaymentStatusColors(paymentStatus)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusColors.first)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = paymentStatus.replace("_", " "),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColors.second
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Total Delivered", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(
                                        text = "${DecimalFormat("#.##").format(totalLiters)} Liters",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Amount Due", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(
                                        text = "Rs. ${DecimalFormat("#.##").format(totalAmount)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Billing actions based on paymentStatus
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (paymentStatus) {
                                    "UNPAID" -> {
                                        Button(
                                            onClick = {
                                                viewModel.requestOrConfirmPaymentByOwner(
                                                    customerId = customer.id,
                                                    monthYear = simulatedMonth,
                                                    isReceived = false
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Request Payment", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.requestOrConfirmPaymentByOwner(
                                                    customerId = customer.id,
                                                    monthYear = simulatedMonth,
                                                    isReceived = true
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Direct Settle", fontSize = 11.sp)
                                        }
                                    }
                                    "PENDING_CONFIRMATION" -> {
                                        Text(
                                            text = "Waiting for customer confirmation...",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                viewModel.requestOrConfirmPaymentByOwner(
                                                    customerId = customer.id,
                                                    monthYear = simulatedMonth,
                                                    isReceived = true
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Mark fully paid", fontSize = 11.sp)
                                        }
                                    }
                                    "CONFIRMED_BY_CUSTOMER" -> {
                                        Button(
                                            onClick = {
                                                viewModel.requestOrConfirmPaymentByOwner(
                                                    customerId = customer.id,
                                                    monthYear = simulatedMonth,
                                                    isReceived = true
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Settle Mutual Payment", fontSize = 11.sp)
                                        }
                                    }
                                    "FULLY_SETTLED" -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Settle complete & logged.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Medium
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
}

// 4. Quantity Change Requests Tab
@Composable
fun OwnerRequestsTab(viewModel: MilkViewModel) {
    val reqs by viewModel.quantityRequests.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Quantity Update Requests",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val pending = reqs.filter { it.status == "PENDING" }
        val processed = reqs.filter { it.status != "PENDING" }

        if (reqs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Edit,
                title = "No Requests",
                desc = "Customer requested milk quantity changes will appear here."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (pending.isNotEmpty()) {
                    item {
                        Text(text = "Pending Approvals", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    items(pending) { req ->
                        val customer = customers.find { it.id == req.customerId }
                        if (customer != null) {
                            QuantityRequestCard(req, customer, onApprove = { viewModel.approveQuantityRequest(req.id) }, onReject = { viewModel.rejectQuantityRequest(req.id) })
                        }
                    }
                }

                if (processed.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Past History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    items(processed) { req ->
                        val customer = customers.find { it.id == req.customerId }
                        if (customer != null) {
                            QuantityRequestCard(req, customer, onApprove = {}, onReject = {}, isProcessed = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuantityRequestCard(
    req: QuantityChangeRequestEntity,
    customer: CustomerEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isProcessed: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "Address: ${customer.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }

                if (isProcessed) {
                    val color = if (req.status == "APPROVED") Color(0xFF2E7D32) else Color(0xFFC62828)
                    Text(
                        text = req.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = color
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "PENDING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Current Supply: ${customer.defaultQuantity}L", fontSize = 11.sp)
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                Text(
                    text = "Requested: ${req.requestedQuantity}L",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Effective Date: ${req.effectiveDateStr}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            if (!isProcessed) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                    ) {
                        Text("Decline", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Approve", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 5. Owner Notifications Tab
@Composable
fun OwnerNotificationsTab(viewModel: MilkViewModel) {
    val notes by viewModel.notifications.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Village Notification Log",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            TextButton(onClick = { viewModel.clearNotifications() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Log", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (notes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Notifications,
                title = "No Notifications",
                desc = "All activities, pause reminders, and status logs will appear here."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { n ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = if (n.title.contains("Pause") || n.title.contains("No Delivery")) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.Notifications
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = n.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(n.timestamp))
                                    Text(text = timeStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = n.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- SCREEN 3: CUSTOMER DASHBOARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(viewModel: MilkViewModel) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val activeCustId by viewModel.activeCustomerId.collectAsStateWithLifecycle()
    val customerTab by viewModel.customerTab.collectAsStateWithLifecycle()

    val currentCustomer = customers.find { it.id == activeCustId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentCustomer?.name ?: "Customer Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                actions = {
                    var showSettings by remember { mutableStateOf(false) }
                    if (showSettings) {
                        SettingsDialog(onDismiss = { showSettings = false })
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Customize Fonts and Colors",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    val context = LocalContext.current
                    TextButton(onClick = { AuthSession.logout(context) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logout")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = customerTab == "DASHBOARD",
                    onClick = { viewModel.customerTab.value = "DASHBOARD" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = customerTab == "QUANTITY_CHANGE",
                    onClick = { viewModel.customerTab.value = "QUANTITY_CHANGE" },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Quantity") },
                    label = { Text("Quantity") }
                )
                NavigationBarItem(
                    selected = customerTab == "BILL_HISTORY",
                    onClick = { viewModel.customerTab.value = "BILL_HISTORY" },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Billing") },
                    label = { Text("Bills") }
                )
                NavigationBarItem(
                    selected = customerTab == "ANNOUNCEMENTS",
                    onClick = { viewModel.customerTab.value = "ANNOUNCEMENTS" },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Notice") },
                    label = { Text("Notice") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (currentCustomer == null) {
                EmptyStateView(
                    icon = Icons.Default.Warning,
                    title = "Profile Error",
                    desc = "Failed to load active customer context. Please return and select a profile."
                )
            } else {
                when (customerTab) {
                    "DASHBOARD" -> CustomerHomeTab(viewModel, currentCustomer)
                    "QUANTITY_CHANGE" -> CustomerQuantityTab(viewModel, currentCustomer)
                    "BILL_HISTORY" -> CustomerBillTab(viewModel, currentCustomer)
                    "ANNOUNCEMENTS" -> CustomerNoticeTab(viewModel, currentCustomer)
                }
            }
        }
    }
}

// 1. Customer Home Dashboard Tab
@Composable
fun CustomerHomeTab(viewModel: MilkViewModel, customer: CustomerEntity) {
    val deliveries by viewModel.dailyDeliveries.collectAsStateWithLifecycle()
    val bills by viewModel.monthlyBills.collectAsStateWithLifecycle()
    val config by viewModel.globalConfig.collectAsStateWithLifecycle()
    val date by viewModel.simulatedDate.collectAsStateWithLifecycle()
    val month by viewModel.simulatedMonth.collectAsStateWithLifecycle()

    var showReminderDialog by remember { mutableStateOf(false) }
    var reminderText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Find today's delivery record
    val todayDelivery = deliveries.find { it.customerId == customer.id && it.dateStr == date }
    val todayStatus = todayDelivery?.deliveryStatus ?: "NOT_DELIVERED"
    val todayQty = todayDelivery?.deliveredQuantity ?: customer.defaultQuantity

    // Monthly volume and estimate
    val currentMonthBill = bills.find { it.customerId == customer.id && it.monthYearStr == month }
    val totalLiters = currentMonthBill?.totalLiters ?: 0.0
    val estAmount = currentMonthBill?.totalAmount ?: 0.0
    val payStatus = currentMonthBill?.status ?: "UNPAID"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Announcement Alert Banner at the very top (Notice Board)
        if (config?.broadcastMessage?.isNotEmpty() == true) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFFBC02D), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Notice",
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Notice from Vendor:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = config!!.broadcastMessage,
                                fontSize = 12.sp,
                                color = Color(0xFF4E342E)
                            )
                        }
                    }
                }
            }
        }

        // Today's Status Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getDeliveryStatusBannerColor(todayStatus)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TODAY'S DELIVERY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getDeliveryStatusMessage(todayStatus, todayQty),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Date: $date • Standard: ${customer.defaultQuantity}L",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Actions: Toggle pause and send reminder
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Quick Delivery Controls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggle Pause Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Do you need milk today?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = if (todayStatus == "PAUSED_BY_CUSTOMER") "You paused your supply today" else "Turn off if you have extra milk in fridge",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Switch: Checked means delivering today. Unchecked means paused by customer.
                        // Wait, if todayStatus is "PAUSED_BY_OWNER", then customer cannot override it to True
                        Switch(
                            checked = todayStatus != "PAUSED_BY_CUSTOMER",
                            onCheckedChange = { isResuming ->
                                if (todayStatus == "PAUSED_BY_OWNER") {
                                    Toast.makeText(context, "Owner has paused deliveries globally today.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.toggleCustomerPauseToday(customer.id, !isResuming)
                                }
                            },
                            enabled = todayStatus != "PAUSED_BY_OWNER"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Reminder Button
                    Button(
                        onClick = { showReminderDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Special Reminder to Owner")
                    }
                }
            }
        }

        // Live Estimation summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Current Month Summary ($month)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Liters Received", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "${DecimalFormat("#.##").format(totalLiters)} Liters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Estimated Cost Due", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "Rs. ${DecimalFormat("#.##").format(estAmount)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mutual status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Bill Payment Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = payStatus.replace("_", " "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Special Reminder Dialog
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Send Pause Reminder / Notice") },
            text = {
                Column {
                    Text(
                        text = "Leave a brief text message detailing why you paused today (e.g. 'Guests left early', 'Away on trip for 2 days').",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = reminderText,
                        onValueChange = { reminderText = it },
                        placeholder = { Text("e.g., Going to city today, please do not bring milk.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reminderText.isNotBlank()) {
                            viewModel.sendPauseReminderToOwner(customer.id, reminderText)
                            reminderText = ""
                            showReminderDialog = false
                            Toast.makeText(context, "Reminder sent to vendor log.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// 2. Customer Quantity Change Tab
@Composable
fun CustomerQuantityTab(viewModel: MilkViewModel, customer: CustomerEntity) {
    val reqs by viewModel.quantityRequests.collectAsStateWithLifecycle()
    var showRequestDialog by remember { mutableStateOf(false) }
    var reqQtyText by remember { mutableStateOf("1.0") }
    var effectiveDateText by remember { mutableStateOf("2026-06-30") }

    val myRequests = reqs.filter { it.customerId == customer.id }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    reqQtyText = customer.defaultQuantity.toString()
                    showRequestDialog = true
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Request Change")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Current Supply Profile",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${customer.defaultQuantity} Liters Daily",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Calculated at Rs.${customer.pricePerLiter}/L. To request a larger or smaller quantity from tomorrow, click the request button below.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "Your Quantity Update Requests",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (myRequests.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "No Requests Yet",
                    desc = "Your submitted quantity requests and their vendor approval status will be listed here."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myRequests) { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Request: Change daily to ${r.requestedQuantity}L",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(text = "Effective: ${r.effectiveDateStr}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }

                                // Status Indicator
                                val statusColor = when (r.status) {
                                    "PENDING" -> WarningOrange
                                    "APPROVED" -> PrimaryGreen
                                    else -> ErrorRed
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = r.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submit Request Dialog
        if (showRequestDialog) {
            AlertDialog(
                onDismissRequest = { showRequestDialog = false },
                title = { Text("Request Supply Quantity Change") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Fill out the fields to submit a change request. This will notify the vendor to approve your new volume.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        OutlinedTextField(
                            value = reqQtyText,
                            onValueChange = { reqQtyText = it },
                            label = { Text("New Daily Quantity (Liters)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = effectiveDateText,
                            onValueChange = { effectiveDateText = it },
                            label = { Text("Effective Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val qty = reqQtyText.toDoubleOrNull()
                            if (qty != null && qty > 0.0 && effectiveDateText.isNotBlank()) {
                                viewModel.submitQuantityChangeRequest(customer.id, qty, effectiveDateText)
                                showRequestDialog = false
                            }
                        }
                    ) {
                        Text("Submit Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRequestDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// 3. Customer Bill & Payment History Tab
@Composable
fun CustomerBillTab(viewModel: MilkViewModel, customer: CustomerEntity) {
    val bills by viewModel.monthlyBills.collectAsStateWithLifecycle()
    val deliveries by viewModel.dailyDeliveries.collectAsStateWithLifecycle()
    val monthYear by viewModel.simulatedMonth.collectAsStateWithLifecycle()

    val myBills = bills.filter { it.customerId == customer.id }
    val currentBill = myBills.find { it.monthYearStr == monthYear }

    val totalLiters = currentBill?.totalLiters ?: 0.0
    val totalAmount = currentBill?.totalAmount ?: 0.0
    val billStatus = currentBill?.status ?: "UNPAID"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Month Bill details
        item {
            Text(
                text = "Current Billing Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "June 2026 Cycle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Price Rate: Rs. ${customer.pricePerLiter}/L", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        // Badge
                        val colors = getPaymentStatusColors(billStatus)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.first)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = billStatus.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.second
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Calculated Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "${DecimalFormat("#.##").format(totalLiters)} Liters", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Estimated Bill", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "Rs. ${DecimalFormat("#.##").format(totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mutual Confirmation Actions
                    if (billStatus == "UNPAID" || billStatus == "PENDING_CONFIRMATION" || billStatus == "CONFIRMED_BY_OWNER") {
                        if (currentBill?.customerConfirmed == true) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "You confirmed payment. Waiting for vendor.",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.confirmPaymentByCustomer(customer.id, monthYear) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("I Have Paid (Confirm Payment)")
                            }
                        }
                    } else if (billStatus == "FULLY_SETTLED") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Settled mutually! Thank you.",
                                color = Color(0xFF2E7D32),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bill History (Past months)
        item {
            Text(
                text = "Previous Cycles History",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val pastBills = myBills.filter { it.monthYearStr != monthYear }
        if (pastBills.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "No Past Bills Found",
                    desc = "Completed historical monthly billing logs will be shown here."
                )
            }
        } else {
            items(pastBills) { b ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Month Cycle: ${b.monthYearStr}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${DecimalFormat("#.##").format(b.totalLiters)}L delivered • Rs. ${DecimalFormat("#.##").format(b.totalAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = b.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. Announcements Notice Tab
@Composable
fun CustomerNoticeTab(viewModel: MilkViewModel, customer: CustomerEntity) {
    val notes by viewModel.notifications.collectAsStateWithLifecycle()
    val myNotes = notes.filter { n -> n.customerId == customer.id || n.customerId <= 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Notice Board & Announcements",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (myNotes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Campaign,
                title = "Notice board is empty",
                desc = "No announcements or notices are posted right now."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(myNotes) { n ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (n.customerId == 0) Color(0xFFFFFDE7) else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (n.customerId == 0) Icons.Default.Campaign else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (n.customerId == 0) Color(0xFFF57C00) else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = n.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    val timeStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.US).format(Date(n.timestamp))
                                    Text(text = timeStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = n.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- REUSABLE COMPOSABLES & UTILITIES ---

@Composable
fun EmptyStateView(icon: ImageVector, title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = desc,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

// Map payment status code strings to beautiful colors
fun getPaymentStatusColors(status: String): Pair<Color, Color> {
    return when (status) {
        "UNPAID" -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // soft red
        "PENDING_CONFIRMATION" -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100)) // soft orange
        "CONFIRMED_BY_CUSTOMER" -> Pair(Color(0xFFE0F7FA), Color(0xFF006064)) // soft teal
        "FULLY_SETTLED" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // soft green
        else -> Pair(Color(0xFFECEFF1), Color(0xFF37474F)) // gray
    }
}

// Map delivery status code strings to beautiful background colors
fun getDeliveryStatusBannerColor(status: String): Color {
    return when (status) {
        "DELIVERED" -> Color(0xFFE8F5E9) // pastoral light green
        "PAUSED_BY_CUSTOMER" -> Color(0xFFFFF3E0) // soft orange
        "PAUSED_BY_OWNER" -> Color(0xFFFFEBEE) // soft warning red
        else -> Color(0xFFECEFF1) // clean neutral gray
    }
}

// Generate delivery status display message
fun getDeliveryStatusMessage(status: String, qty: Double): String {
    val qStr = DecimalFormat("#.##").format(qty)
    return when (status) {
        "DELIVERED" -> "Delivered: $qStr Liters Today"
        "PAUSED_BY_CUSTOMER" -> "Supply Paused by You"
        "PAUSED_BY_OWNER" -> "Cancelled by Vendor Today"
        else -> "Pending Delivery Details"
    }
}
