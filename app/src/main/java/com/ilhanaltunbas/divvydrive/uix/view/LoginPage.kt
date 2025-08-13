package com.ilhanaltunbas.divvydrive.uix.view

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ilhanaltunbas.divvydrive.MainActivity
import com.ilhanaltunbas.divvydrive.R
import com.ilhanaltunbas.divvydrive.ui.theme.AcikSiyah
import com.ilhanaltunbas.divvydrive.ui.theme.KayitOl
import com.ilhanaltunbas.divvydrive.ui.theme.LoginBorder
import com.ilhanaltunbas.divvydrive.ui.theme.LoginText
import com.ilhanaltunbas.divvydrive.ui.theme.MaviLogo
import com.ilhanaltunbas.divvydrive.uix.state.LoginState
import com.ilhanaltunbas.divvydrive.uix.viewmodel.LoginPageViewModel
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(navController: NavController,
              loginPageViewModel: LoginPageViewModel = hiltViewModel()
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val loginState by loginPageViewModel.loginstate.collectAsState()

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()



    LaunchedEffect(loginState) {
        when (val currentState = loginState) {
            is LoginState.Success -> {

                navController.navigate("mainPage") {
                    popUpTo("login") { inclusive = true }
                    launchSingleTop = true
                }

                Log.d("ComposableDebug", "Navigasyon çağrısı yapıldı.")
                loginPageViewModel.consumeLoginState() // State'i tüket
            }
            is LoginState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = currentState.message,
                        duration = SnackbarDuration.Short
                    )
                }
                loginPageViewModel.consumeLoginState()
            }
            else -> { /* Idle  */ }
        }
    }


    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFf1f2f3), Color(0xFF2575FC)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    val gradientBrush2 = Brush.linearGradient(
        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    val commonCornerRadius = 12.dp

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = gradientBrush)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                        .padding(start = 20.dp, end = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(bottom = 40.dp)
                    ) {

                        Icon(
                            painter = painterResource(R.drawable.logo),
                            tint = Color.Unspecified,
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(16.dp)),
                        )

                        Spacer(modifier = Modifier.padding(8.dp))
                        Row {
                            Text(
                                text = "DIVVY",
                                color = AcikSiyah,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "DRIVE",
                                color = MaviLogo,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Güvenle Sakla, Her Yerde Eriş.",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(text = "Kullanıcı adı") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Kullanıcı adı iconu"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(commonCornerRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LoginText,
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = LoginBorder,
                                focusedLabelColor = LoginText,
                                unfocusedLabelColor = LoginText,
                                cursorColor = LoginText,
                                unfocusedPlaceholderColor = LoginText,
                            )

                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(text = "Şifre") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Şifre iconu"
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    Icons.Outlined.Visibility
                                else Icons.Outlined.VisibilityOff
                                val description = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(commonCornerRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LoginText,
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = LoginBorder,
                                focusedLabelColor = LoginText,
                                unfocusedLabelColor = LoginText,
                                cursorColor = LoginText,
                                unfocusedPlaceholderColor = LoginText,
                            )


                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(commonCornerRadius))
                                .background(brush = gradientBrush2),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    if (username.isNotBlank() && password.isNotBlank()) {
                                        loginPageViewModel.girisYap(username, password)
                                    } else {
                                        // Snackbar
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Kullanıcı adı ve şifre boş olamaz.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }

                                },
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(commonCornerRadius),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White
                                ),
                                enabled = loginState !is LoginState.Loading
                            ) {
                                if (loginState is LoginState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Text(text = "Login", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier
                            .fillMaxWidth(0.80f)
                            .padding(start = 8.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Şifremi unuttum",
                                color = Color.Gray,
                                fontSize = 12.sp)
                            Text(text = "Kayıt Ol",
                                color = KayitOl,
                                fontSize = 12.sp
                            )
                        }

                    }
                }
            }
    }
}

@Preview
@Composable
fun LoginPagePreview() {
    val navControllertest = NavController(
        context = MainActivity()
    )
    LoginPage(navController = navControllertest)
}