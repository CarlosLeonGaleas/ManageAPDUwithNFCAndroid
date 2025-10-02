package com.example.myndef.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myndef.MainActivityViewModel
import com.example.myndef.MessageManager
import com.example.myndef.R
import kotlinx.coroutines.launch

// Colores institucionales
private val InstitutionalBlue = Color(0xFF27348B)
private val InstitutionalOrange = Color(0xFFF3940B)
private val LightGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    nameText: String,
    phoneNumber: String,
    lastLogin: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPhoneNumberValid: (Boolean) -> Unit,
    isPhoneNumberValid: Boolean,
    viewModel: MainActivityViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun onLogin() {
        if (nameText.isNotEmpty() && phoneNumber.isNotEmpty() && isPhoneNumberValid) {
            MessageManager.setMessage("$nameText|$phoneNumber")
            viewModel.updateLastLogin(MessageManager.getMessage())
            viewModel.updateIsLogged(true)
        }
        else if (nameText.isEmpty() || phoneNumber.isEmpty()){
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Por favor ingrese todos los datos para ingresar")
            }
        } else if (!isPhoneNumberValid){
            coroutineScope.launch {
                snackbarHostState.showSnackbar("El número de teléfono ingresado contiene errores")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo izquierdo (Universitario Rumiñahui)
                        Image(
                            painter = painterResource(R.drawable.universitario_ru_blanco),
                            contentDescription = "Logo Universitario RU",
                            modifier = Modifier.width(180.dp),
                            contentScale = ContentScale.Fit
                        )

                        // Logo derecho (Departamento de Investigación)
                        Image(
                            painter = painterResource(R.drawable.departamentoinv_blanco),
                            contentDescription = "Logo Investigación",
                            modifier = Modifier.height(40.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InstitutionalBlue
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Logo de la aplicación en forma circular
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = InstitutionalBlue,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logoblanco),
                    contentDescription = "Logo de la Aplicación",
                    modifier = Modifier.size(100.dp)
                )
            }

            // Título de bienvenida
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = InstitutionalBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sistema de Cuestionarios NFC",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Card con formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InstitutionalBlue
                    )

                    // Campo de nombre
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = onNameChange,
                        label = { Text("Nombre completo") },
                        leadingIcon = {
                            Image(
                                painter = painterResource(R.drawable.person_user),
                                contentDescription = "Icono de Usuario",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InstitutionalBlue,
                            focusedLabelColor = InstitutionalBlue,
                            cursorColor = InstitutionalBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Campo de teléfono
                    PhoneNumberInput(
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = onPhoneChange,
                        onPhoneNumberValid = onPhoneNumberValid
                    )

                    // Botón de login
                    Button(
                        onClick = { onLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InstitutionalOrange
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.login),
                            contentDescription = "Ingresar",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Registrar e Ingresar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Card de último registro
            val parts = lastLogin.split("|")
            val nombreCompleto = parts.getOrNull(0) ?: ""
            val numeroTelefonico = parts.getOrNull(1) ?: ""

            fun useLastDataLogin() {
                onNameChange(nombreCompleto)
                onPhoneChange(numeroTelefonico)
                onPhoneNumberValid(true)
            }

            if (nombreCompleto.isNotEmpty() && numeroTelefonico.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.history_user),
                                contentDescription = "último registro",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Último registro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InstitutionalBlue
                            )
                        }

                        // Información del último usuario
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = LightGray
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Nombre
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.person_user),
                                        contentDescription = "Icono de Usuario",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Nombre",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = nombreCompleto,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }

                                // Teléfono
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.mobile_phone),
                                        contentDescription = "Icono de Teléfono",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Teléfono",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = numeroTelefonico,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        // Botón de reutilización
                        Button(
                            onClick = { useLastDataLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InstitutionalBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_up),
                                contentDescription = "Usar estos datos",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Usar estos datos",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onPhoneNumberValid: (Boolean) -> Unit
) {
    val maxDigits = 10
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hasBeenFocused by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    // Inicializar con "09" si está vacío
    LaunchedEffect(phoneNumber) {
        if (phoneNumber.isEmpty()) {
            onPhoneNumberChange("09")
        }
    }

    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { newValue ->
            val numericInput = newValue.filter { it.isDigit() }
            if (numericInput.length <= maxDigits) {
                onPhoneNumberChange(numericInput)
                onPhoneNumberValid(false)
            }
            if (numericInput.length == maxDigits) {
                if (!numericInput.startsWith("09")) {
                    isError = true
                    errorMessage = "El número debe comenzar con 09"
                    onPhoneNumberValid(false)
                } else {
                    isError = false
                    onPhoneNumberValid(true)
                    errorMessage = ""
                }
            }
        },
        label = { Text("Número de teléfono") },
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.mobile_phone),
                    contentDescription = "Icono de Teléfono",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+593",
                    fontWeight = FontWeight.Medium,
                    color = InstitutionalBlue
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    hasBeenFocused = true
                    isError = false
                    onPhoneNumberValid(false)
                    errorMessage = ""
                } else if (hasBeenFocused) {
                    if (!phoneNumber.startsWith("09")) {
                        isError = true
                        errorMessage = "El número debe comenzar con 09"
                    } else if (phoneNumber.length != maxDigits) {
                        isError = true
                        errorMessage = "El número debe tener exactamente 10 dígitos"
                    } else {
                        isError = false
                        onPhoneNumberValid(true)
                        errorMessage = ""
                    }
                }
            },
        isError = isError,
        supportingText = if (isError) {
            {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InstitutionalBlue,
            focusedLabelColor = InstitutionalBlue,
            cursorColor = InstitutionalBlue,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp)
    )
}