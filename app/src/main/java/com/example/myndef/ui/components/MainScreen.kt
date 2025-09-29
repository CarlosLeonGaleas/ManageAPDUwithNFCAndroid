package com.example.myndef.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.example.myndef.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myndef.MainActivityViewModel
import com.example.myndef.MyHostApduService
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    name: String,
    phone: String,
    apduCommand: String,
    onLogout: () -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {
    val status by viewModel.status.collectAsState()
    val q1 by viewModel.q1.collectAsState()
    val optionQ1Selected by viewModel.q1Selected.collectAsState()
    val q2 by viewModel.q2.collectAsState()
    val optionQ2Selected by viewModel.q2Selected.collectAsState()
    val q3 by viewModel.q3.collectAsState()
    val optionQ3Selected by viewModel.q3Selected.collectAsState()
    val q4 by viewModel.q4.collectAsState()
    val optionQ4Selected by viewModel.q4Selected.collectAsState()
    val q5 by viewModel.q5.collectAsState()
    val optionQ5Selected by viewModel.q5Selected.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun onConfirm() {
        if (optionQ1Selected == null || optionQ2Selected == null || optionQ3Selected == null || optionQ4Selected == null || optionQ5Selected == null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Por favor, responda todas las preguntas")
            }
            return
        }
        else{
            if (status != "CONFIRMED") {
                MainActivityViewModel.instance?.updateStatusFase("QUIZ_CONFIRMED")
                viewModel.updateStatus("CONFIRMED")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Acerque su teléfono al lector para mostrar y registrar su puntuación")
                }
            }
            else{
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Acerque su teléfono al lector para mostrar y registrar su puntuación")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserCard(name = name, phone = phone, onLogout = onLogout)

            if (status == "EMPTY") {
                Text("Acerque su teléfono para acceder al cuestionario")
            }

            ApduCard(apduCommand)

            if (status == "COMPLETED" || status == "CONFIRMED") {
                OptionsQuestion(
                    question = q1,
                    enabled = status == "COMPLETED",
                    optionSelected = optionQ1Selected,
                    updateSelected = { viewModel.updateQ1Selected(it) }
                )
                OptionsQuestion(
                    question = q2,
                    enabled = status == "COMPLETED",
                    optionSelected = optionQ2Selected,
                    updateSelected = { viewModel.updateQ2Selected(it) }
                )
                OptionsQuestion(
                    question = q3,
                    enabled = status == "COMPLETED",
                    optionSelected = optionQ3Selected,
                    updateSelected = { viewModel.updateQ3Selected(it) }
                )
                OptionsQuestion(
                    question = q4,
                    enabled = status == "COMPLETED",
                    optionSelected = optionQ4Selected,
                    updateSelected = { viewModel.updateQ4Selected(it) }
                )
                OptionsQuestion(
                    question = q5,
                    enabled = status == "COMPLETED",
                    optionSelected = optionQ5Selected,
                    updateSelected = { viewModel.updateQ5Selected(it) }
                )

                Button(onClick = { onConfirm() }) {
                    Text("Confirmar Respuestas")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.save_responses),
                        contentDescription = "Confirmar Respuestas",
                    )
                }

                if (status == "CONFIRMED") {
                    Text("Acerque su teléfono al lector para ver y registrar su puntuación")
                }
            }
        }
    }
}


@Composable
fun UserCard(name: String, phone: String, onLogout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Nombre: $name")
                Text("Teléfono: $phone")
            }
            Button(onClick = onLogout) {
                Icon(
                    painter = painterResource(R.drawable.logout),
                    contentDescription = "Cerrar Sesión",
                )
            }
        }
    }
}


@Composable
fun ApduCard(command: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("APDU Command:")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = command, fontSize = 12.sp)
        }
    }
}

@Composable
fun OptionsQuestion(
    modifier: Modifier = Modifier,
    question: String,
    enabled: Boolean = true,
    optionSelected: Int?,
    updateSelected: (Int?) -> Unit
) {
    val questionSplited = question.split("|")
    val radioOptions = listOf(questionSplited[1], questionSplited[2], questionSplited[3], questionSplited[4])
    // val (selectedOption, onOptionSelected) = remember(question) { mutableStateOf(radioOptions[0]) }
    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(modifier.selectableGroup()) {
        Text(questionSplited[0])
        radioOptions.forEachIndexed { index, text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (index == optionSelected),
                        onClick = {
                            if (enabled) {
                                updateSelected(index)
                            } else null
                        },
                        role = Role.RadioButton,
                        enabled = enabled
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (index == optionSelected),
                    onClick = null, // null recommended for accessibility with screen readers
                    enabled = enabled
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}


