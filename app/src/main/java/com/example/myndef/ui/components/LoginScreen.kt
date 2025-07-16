package com.example.myndef.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myndef.R

@Composable
fun LoginScreen(
    nameText: String,
    phoneNumber: String,
    statusText: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onPhoneNumberValid: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.departamentoinv),
            contentDescription = "Departamento Inv",
            modifier = Modifier
                .fillMaxWidth()
                .height(99.dp)
        )

        OutlinedTextField(
            value = nameText,
            onValueChange = onNameChange,
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        PhoneNumberInput(phoneNumber = phoneNumber, onPhoneNumberChange = onPhoneChange, onPhoneNumberValid = onPhoneNumberValid)

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar e Ingresar")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.login),
                contentDescription = "Ingresar",
            )
        }

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
            Text("+593", modifier = Modifier.padding(start = 8.dp))
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
                    // Solo validar si el usuario alguna vez tocó el campo
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
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}



