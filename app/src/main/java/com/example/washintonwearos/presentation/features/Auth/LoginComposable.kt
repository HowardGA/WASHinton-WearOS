package com.example.washintonwearos.presentation.features.Auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.example.washintonwearos.R
import com.example.washintonwearos.presentation.theme.LightBlue
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Button
import com.example.washintonwearos.presentation.theme.DarkBlue
import androidx.wear.compose.material.Scaffold



@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val viewModel = LoginViewModel()
    val uiState = viewModel.state.collectAsState()
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()

    LaunchedEffect(key1 = uiState.value) {
        when (uiState.value) {
            is SignInState.Success -> {
                Log.d("LoginScreen", "Login success. Navigating to home.")
                navController.navigate("home")
            }
            is SignInState.Error -> {
                Log.e("LoginScreen", "Login error encountered.")
                Toast.makeText(context, "Login failed! Please try again.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.d("LoginScreen", "State: ${uiState.value}")
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(DarkBlue),
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.notification_icon),
                    contentDescription = "WASHinton Logo",
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }
            item {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }
            item {
                Button(
                    onClick = {
                        Log.d("LoginScreen", "Sign-in button clicked with email: $email")
                        viewModel.signIn(email, password)
                    },
                    enabled = email.isNotEmpty() && password.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(LightBlue)
                ) {
                    Text("Sign In", color = Color.White)
                }
            }
            if (uiState.value == SignInState.Loading) {
                item { CircularProgressIndicator() }
            }
        }
    }
}
