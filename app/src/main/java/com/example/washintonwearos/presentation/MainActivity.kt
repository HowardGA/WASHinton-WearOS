
package com.example.washintonwearos.presentation

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.washintonwearos.presentation.features.DataClasses.TransferDetail
import com.example.washintonwearos.presentation.features.DataClasses.TransferOrder
import com.example.washintonwearos.presentation.features.ViewModels.OrdersViewModel
import com.example.washintonwearos.presentation.theme.GreenCheck
import com.example.washintonwearos.presentation.theme.InfoYellow
import com.example.washintonwearos.presentation.theme.LightBlue
import com.example.washintonwearos.presentation.theme.RedLogOut
import com.example.washintonwearos.presentation.theme.WashintonWearOSTheme
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import com.example.washintonwearos.presentation.features.Auth.LoginScreen
import com.example.washintonwearos.presentation.theme.DarkBlue
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("FCM", "Notification permission granted.")
            } else {
                Log.e("FCM", "Notification permission denied.")
            }
        }

    private val ordersViewModel: OrdersViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {


        FirebaseMessaging.getInstance().subscribeToTopic("warehouse_transfers")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to topic: warehouse_transfers")
                } else {
                    Log.e("FCM", "Topic subscription failed")
                }
            }

        // Request POST_NOTIFICATIONS permission if Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()  // Cast explicitly

            // Check if there's an orderID from the notification
            val transferId = intent?.getIntExtra("transfer_id", -1)

            WearApp(navController, ordersViewModel)

            // Navigate to the DetailedList screen after NavHost is initialized
            LaunchedEffect(transferId) {
                if (transferId != null && transferId != -1) {
                    navController.navigate("details/$transferId")
                }
            }
        }
    }

    }



private const val TAG = "ActionButtons"
private const val MESSAGE_PATH = "/deploy"

//Rejected

@Composable
fun WearApp(navController: NavController,ordersViewModel: OrdersViewModel) {
    val orders by ordersViewModel.orders
    WashintonWearOSTheme {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val start = if (currentUser != null) "home" else "login"
        NavHost(navController = navController as NavHostController, startDestination = start) {
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(Screen.Home.route) {
                // Refresh orders when navigating to Home
                LaunchedEffect(Unit) {
                    ordersViewModel.fetchOrders()
                }
                HomeList(navController, orders)
            }
            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("transfer_id") { type = NavType.IntType })
            ) { backStackEntry ->
                val transferId = backStackEntry.arguments?.getInt("transfer_id")
                if (transferId != null) {
                    DetailedList(navController, ordersViewModel, transferId)
                }
            }
        }
    }
}


@Composable
fun HomeList(navController: NavController, orders: List<TransferOrder>) {
    // Remember scroll state for rotary input
    val listState = rememberScalingLazyListState()

    Scaffold(
        modifier = Modifier.background(DarkBlue),
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState) // Scroll position indicator
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Pending Approval Orders",
                    fontSize = MaterialTheme.typography.title3.fontSize,
                    fontWeight = MaterialTheme.typography.title3.fontWeight,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
            items(orders) { order ->
                OrderCard(navController, order)
            }
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun DetailedList(navController: NavController, ordersViewModel: OrdersViewModel, orderID: Int) {
    val orderDetails by ordersViewModel.orderDetails
    val details = orderDetails?.details ?: emptyList()

    // Remember scroll state for rotary input
    val listState = rememberScalingLazyListState()

    // Swipeable state
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(0f to 0, 300f to 1) // Anchor points for swipe gesture

    LaunchedEffect(orderID) {
        ordersViewModel.fetchOrderDetails(orderID)
    }

    Scaffold(
        modifier = Modifier.background(DarkBlue),
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState) // Scroll position indicator
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                )
        ) {
            ScalingLazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "List Of Products for Order $orderID",
                        fontSize = MaterialTheme.typography.title3.fontSize,
                        fontWeight = MaterialTheme.typography.title3.fontWeight,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    )
                }

                items(details) { detail ->
                    OrderCardDetails(detail)
                }

                item {
                    ActionButtons(navController, ordersViewModel, orderID)
                }
            }

            // Navigate back when swipe state changes
            LaunchedEffect(swipeableState.currentValue) {
                if (swipeableState.currentValue == 1) {
                    navController.popBackStack()
                }
            }
        }
    }
}



@Composable
fun ActionButtons(navController: NavController, ordersViewModel: OrdersViewModel, orderID: Int) {
    val message by ordersViewModel.message.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Rejecting order...")
                        ordersViewModel.rejectOrder(orderID)
                        ordersViewModel.notifyApp("rejected",orderID)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue),
                modifier = Modifier.width(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject",
                    tint = RedLogOut,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Approving order...")
                        ordersViewModel.approveOrder(orderID)
                        ordersViewModel.notifyApp("approved",orderID)

                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue),
                modifier = Modifier.width(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept",
                    tint = GreenCheck,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Snackbar Host
        SnackbarHost(hostState = snackbarHostState)

        // Navigate and show snackbar after message is updated
        message?.let {
            LaunchedEffect(it) {
                snackbarHostState.showSnackbar(it)
                delay(2000) // Wait for snackbar to be visible
                navController.navigate(Screen.Home.route) // Navigate back to home
                ordersViewModel.clearMessage() // Clear the message after handling
            }
        }
    }
}



@Composable
fun OrderCardDetails(detail: TransferDetail) {
    Card(
        onClick = { /* No action yet */ },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quantity: ${detail.quantity}",
                    fontSize = 13.sp,
                    fontWeight = MaterialTheme.typography.title3.fontWeight,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Price: ${detail.product.price}",
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = LightBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = detail.product.name,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun OrderCard(navController: NavController, order: TransferOrder) {
    Card(
        onClick = {navController.navigate(Screen.Details.createRoute(order.transfer_id))},
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order Number: ${order.transfer_id}",
                    fontSize = 13.sp,
                    fontWeight = MaterialTheme.typography.title3.fontWeight,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.weight(1f)
                )

            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = LightBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = order.store,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Details : Screen("details/{transfer_id}") {
        fun createRoute(transferId: Int) = "details/$transferId"
    }
}



//@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearApp()
//}
