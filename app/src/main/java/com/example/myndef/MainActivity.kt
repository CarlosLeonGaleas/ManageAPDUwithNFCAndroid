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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myndef.ui.theme.MyNDEFTheme

class MainActivity : ComponentActivity() {
    private val apduReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val command = intent?.getStringExtra("APDU_COMMAND") ?: "Unknown"
            // Actualizar el ViewModel cuando se reciba un comando
            MainViewModel.instance?.updateRequestText(command)
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
                    MainScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el BroadcastReceiver
        unregisterReceiver(apduReceiver)
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados observables del ViewModel
    val actualMessage by viewModel.actualMessage.collectAsState()
    val nameText by viewModel.nameText.collectAsState()
    val phoneText by viewModel.phoneText.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val requestText by viewModel.requestText.collectAsState()

    // Actualizar el mensaje actual cuando cambie
    LaunchedEffect(Unit) {
        viewModel.updateActualMessage(MessageManager.getMessage())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ImageView equivalente
        Image(
            painter = painterResource(id = R.drawable.departamentoinv),
            contentDescription = "Departamento Inv",
            modifier = Modifier
                .fillMaxWidth()
                .height(99.dp)
        )

        // Mensaje actual
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = actualMessage,
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        }

        // Campo de nombre
        OutlinedTextField(
            value = nameText,
            onValueChange = { viewModel.updateNameText(it) },
            label = { Text("Escriba su nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3
        )

        // Campo de teléfono
        OutlinedTextField(
            value = phoneText,
            onValueChange = { viewModel.updatePhoneText(it) },
            label = { Text("Escriba su número de teléfono") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 3
        )

        // Botón de inicio de registro
        Button(
            onClick = {
                if (nameText.isNotEmpty() && phoneText.isNotEmpty()) {
                    MessageManager.setMessage(nameText + phoneText)
                    viewModel.updateStatusText("Acerque su teléfono al lector NFC")
                    viewModel.updateActualMessage(MessageManager.getMessage())
                } else {
                    viewModel.updateStatusText("Por favor ingrese todos los datos para el iniciar el registro")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar registro")
        }

        // Estado del registro
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = statusText,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }

        // Comando APDU
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "APDU Command:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = requestText,
                    fontSize = 12.sp
                )
            }
        }
    }
}