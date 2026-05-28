package com.gamestore.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.gamestore.ui.screen.auth.AuthScreen
import com.gamestore.ui.screen.cart.CartScreen
import com.gamestore.ui.screen.detail.DetailScreen
import com.gamestore.ui.screen.home.HomeScreen
import com.gamestore.ui.screen.order.OrderHistoryScreen
import com.gamestore.ui.screen.order.OrderSuccessScreen
import com.gamestore.ui.screen.profile.ProfileScreen
import com.gamestore.ui.theme.*


object R {
    const val HOME    = "home"
    const val DETAIL  = "detail/{gameId}"
    const val CART    = "cart"
    const val SUCCESS = "success/{orderId}"
    const val ORDERS  = "orders"
    const val PROFILE = "profile"
    const val AUTH    = "auth"
    fun detail(id: Int)   = "detail/$id"
    fun success(id: Int)  = "success/$id"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    val tabs = listOf(
        Triple(R.HOME,    "Trang chủ", Icons.Default.Home),
        Triple(R.CART,    "Giỏ hàng",  Icons.Default.ShoppingCart),
        Triple(R.ORDERS,  "Đơn hàng",  Icons.Default.Receipt),
        Triple(R.PROFILE, "Tài khoản", Icons.Default.Person),
    )
    val noBar = setOf("detail/", "success/", R.AUTH)
    val showBar = noBar.none { route?.startsWith(it.trimEnd('/')) == true }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (showBar) NavigationBar(containerColor = DarkSurf) {
                val dest = entry?.destination
                tabs.forEach { (tabRoute, label, icon) ->
                    NavigationBarItem(
                        selected = dest?.hierarchy?.any { it.route == tabRoute } == true,
                        onClick  = {
                            nav.navigate(tabRoute) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        },
                        icon   = { Icon(icon, null) },
                        label  = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PurpleLt, selectedTextColor = PurpleLt,
                            unselectedIconColor = TextMuted, unselectedTextColor = TextMuted,
                            indicatorColor = DarkCard,
                        ),
                    )
                }
            }
        }
    ) { padding ->
        NavHost(nav, R.HOME, Modifier.padding(padding)) {
            composable(R.HOME) {
                HomeScreen(onGameClick = { nav.navigate(R.detail(it)) }, onCartClick = { nav.navigate(R.CART) })
            }
            composable(R.DETAIL, listOf(navArgument("gameId") { type = NavType.IntType })) {
                DetailScreen(onBack = { nav.popBackStack() }, onCartClick = { nav.navigate(R.CART) })
            }
            composable(R.CART) {
                CartScreen(onBack = { nav.popBackStack() }, onOrderSuccess = { nav.navigate(R.success(it)) { popUpTo(R.CART) { inclusive = true } } })
            }
            composable(R.SUCCESS, listOf(navArgument("orderId") { type = NavType.IntType })) { back ->
                val id = back.arguments?.getInt("orderId") ?: 0
                OrderSuccessScreen(id, onGoHome = { nav.navigate(R.HOME) { popUpTo(0) { inclusive = true } } }, onViewOrders = { nav.navigate(R.ORDERS) { popUpTo(R.HOME) } })
            }
            composable(R.ORDERS)  { OrderHistoryScreen(onBack = { nav.popBackStack() }) }
            composable(R.PROFILE) { ProfileScreen(onLoginClick = { nav.navigate(R.AUTH) }, onOrderHistoryClick = { nav.navigate(R.ORDERS) }) }
            composable(R.AUTH)    { AuthScreen(onSuccess = { nav.popBackStack() }) }
        }
    }
}