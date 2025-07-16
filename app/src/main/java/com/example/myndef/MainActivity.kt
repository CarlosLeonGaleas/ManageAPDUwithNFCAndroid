package com.example.myndef

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myndef.ui.components.LoginScreen
import com.example.myndef.ui.components.MainScreen
import com.example.myndef.ui.components.MainScreenViewModel
import com.example.myndef.ui.theme.MyNDEFTheme

class MainActivity : ComponentActivity() {
    private val apduReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val command = intent?.getStringExtra("APDU_COMMAND") ?: "Unknown"

            // Actualizar el ViewModel cuando se reciba un comando
            when {
                command.startsWith("UPDATE_Q1:") -> {
                    val value = command.substringAfter("UPDATE_Q1:")
                    MainScreenViewModel.instance?.updateQ1(value)
                }
                command.startsWith("UPDATE_Q2:") -> {
                    val value = command.substringAfter("UPDATE_Q2:")
                    MainScreenViewModel.instance?.updateQ2(value)
                }
                command.startsWith("UPDATE_Q3:") -> {
                    val value = command.substringAfter("UPDATE_Q3:")
                    MainScreenViewModel.instance?.updateQ3(value)
                }
                command.startsWith("UPDATE_Q4:") -> {
                    val value = command.substringAfter("UPDATE_Q4:")
                    MainScreenViewModel.instance?.updateQ4(value)
                }
                command.startsWith("UPDATE_Q5:") -> {
                    val value = command.substringAfter("UPDATE_Q5:")
                    MainScreenViewModel.instance?.updateQ5(value)
                }
                command.startsWith("NFC Desconectado") -> {
                    MainActivityViewModel.instance?.updateStatusText("NFC Desconectado")
                    MainActivityViewModel.instance?.updateRequestText("Conexión perdida")
                }
                else -> {
                    // Comando genérico - actualizar requestText como antes
                    MainActivityViewModel.instance?.updateRequestText(command)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar MessageManager con el contexto de la actividad
        MessageManager.initialize(this)

        // Registrar el BroadcastReceiver
        registerReceiver(apduReceiver, IntentFilter("APDU_COMMAND_RECEIVED"), RECEIVER_EXPORTED)

        setContent {
            MyNDEFTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrincipalScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el BroadcastReceiver
        try {
            unregisterReceiver(apduReceiver)
        } catch (e: Exception) {
            // Ignorar si ya estaba desregistrado
        }
    }
}

@Composable
fun PrincipalScreen(viewModel: MainActivityViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados observables del ViewModel
    val actualMessage by viewModel.actualMessage.collectAsState()
    val nameText by viewModel.nameText.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val requestText by viewModel.requestText.collectAsState()
    val phoneNumberValid by viewModel.phoneNumberValid.collectAsState()

    var isLoggedIn by remember { mutableStateOf(false) }

    // Actualizar el mensaje actual cuando cambie
    LaunchedEffect(Unit) {
        viewModel.updateActualMessage(MessageManager.getMessage())
    }

    if (!isLoggedIn) {
        LoginScreen(
            nameText = nameText,
            phoneNumber = phoneNumber,
            statusText = statusText,
            onNameChange = viewModel::updateNameText,
            onPhoneChange = viewModel::updatePhoneNumber,
            onPhoneNumberValid = viewModel::updatePhoneNumberValid,
            onLoginClick = {
                if (!phoneNumberValid){
                    viewModel.updateStatusText("El número de teléfono ingresado contiene errores")
                }
                else if (nameText.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    MessageManager.setMessage("$nameText $phoneNumber")
                    viewModel.updateStatusText("Acerque su teléfono al lector NFC")
                    viewModel.updateActualMessage(MessageManager.getMessage())
                    isLoggedIn = true
                } else {
                    viewModel.updateStatusText("Por favor ingrese todos los datos para el iniciar el registro")
                }
            }
        )
    } else {
        MainScreen(
            name = nameText,
            phone = phoneNumber,
            apduCommand = requestText,
            onLogout = {
                isLoggedIn = false
                MessageManager.setMessage("No se ha iniciado sesión")
                viewModel.updateActualMessage(MessageManager.getMessage())
                viewModel.updateStatusText("")
            }
        )
    }
}