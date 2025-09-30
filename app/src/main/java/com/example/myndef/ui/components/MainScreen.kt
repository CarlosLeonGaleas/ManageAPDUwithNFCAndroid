package com.example.myndef.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.example.myndef.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myndef.MainActivityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    name: String,
    phone: String,
    apduCommand: String,
    onLogout: () -> Unit,
    mainViewModel: MainActivityViewModel,
    viewModel: MainScreenViewModel = viewModel()
) {
    val fase by mainViewModel.statusFase.collectAsState()
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

    var showDialog by remember { mutableStateOf(false) }
    var aciertos by remember { mutableStateOf(0) }

    // Extraer el número total de fragmentos y calcular el progreso
    val progressData = remember(fase) {
        calculateProgress(fase)
    }

    // Control de visibilidad para las animaciones
    var showQuestions by remember { mutableStateOf(false) }

    LaunchedEffect(fase, status) {
        if (fase == "QUIZ_RECIBIDO" || status == "CONFIRMED") {
            showQuestions = false
            delay(50) // Pequeño delay para resetear la animación
            showQuestions = true
        }
    }

    fun calcularAciertos(): Int {
        var correctas = 0

        if (optionQ1Selected != null) {
            val correctIndex = q1.split("|").getOrNull(5)?.toIntOrNull() ?: -1
            if (optionQ1Selected == correctIndex) correctas++
        }

        if (optionQ2Selected != null) {
            val correctIndex = q2.split("|").getOrNull(5)?.toIntOrNull() ?: -1
            if (optionQ2Selected == correctIndex) correctas++
        }

        if (optionQ3Selected != null) {
            val correctIndex = q3.split("|").getOrNull(5)?.toIntOrNull() ?: -1
            if (optionQ3Selected == correctIndex) correctas++
        }

        if (optionQ4Selected != null) {
            val correctIndex = q4.split("|").getOrNull(5)?.toIntOrNull() ?: -1
            if (optionQ4Selected == correctIndex) correctas++
        }

        if (optionQ5Selected != null) {
            val correctIndex = q5.split("|").getOrNull(5)?.toIntOrNull() ?: -1
            if (optionQ5Selected == correctIndex) correctas++
        }

        return correctas
    }

    fun onConfirm() {
        if (optionQ1Selected == null || optionQ2Selected == null || optionQ3Selected == null || optionQ4Selected == null || optionQ5Selected == null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Por favor, responda todas las preguntas")
            }
            return
        }
        else{
            aciertos = calcularAciertos()
            showDialog = true

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

            // Mostrar ProgressBar cuando la fase contiene "QUIZ_INICIADO" o "QUIZ_FRAGMENTO"
            if (progressData.showProgress) {
                LinearProgressIndicator(
                    progress = progressData.progress,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Recibiendo fragmentos... ${progressData.fragmentosRecibidos}/${progressData.totalFragmentos}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (fase == "QUIZ_RECIBIDO" || status == "CONFIRMED") {
                AnimatedVisibility(
                    visible = showQuestions,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 600, delayMillis = 0)
                    ) + fadeIn(animationSpec = tween(durationMillis = 600))
                ) {
                    OptionsQuestion(
                        question = q1,
                        enabled = status == "COMPLETED",
                        optionSelected = optionQ1Selected,
                        updateSelected = { viewModel.updateQ1Selected(it) }
                    )
                }

                AnimatedVisibility(
                    visible = showQuestions,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 600, delayMillis = 100)
                    ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 100))
                ) {
                    OptionsQuestion(
                        question = q2,
                        enabled = status == "COMPLETED",
                        optionSelected = optionQ2Selected,
                        updateSelected = { viewModel.updateQ2Selected(it) }
                    )
                }

                AnimatedVisibility(
                    visible = showQuestions,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 600, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 200))
                ) {
                    OptionsQuestion(
                        question = q3,
                        enabled = status == "COMPLETED",
                        optionSelected = optionQ3Selected,
                        updateSelected = { viewModel.updateQ3Selected(it) }
                    )
                }

                AnimatedVisibility(
                    visible = showQuestions,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 600, delayMillis = 300)
                    ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 300))
                ) {
                    OptionsQuestion(
                        question = q4,
                        enabled = status == "COMPLETED",
                        optionSelected = optionQ4Selected,
                        updateSelected = { viewModel.updateQ4Selected(it) }
                    )
                }

                AnimatedVisibility(
                    visible = showQuestions,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 600, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = 400))
                ) {
                    OptionsQuestion(
                        question = q5,
                        enabled = status == "COMPLETED",
                        optionSelected = optionQ5Selected,
                        updateSelected = { viewModel.updateQ5Selected(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = showQuestions,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 400, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = 400))
                ) {
                    Button(
                        onClick = { onConfirm() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar Respuestas")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(R.drawable.save_responses),
                            contentDescription = "Confirmar Respuestas",
                        )
                    }
                }

                if (status == "CONFIRMED") {
                    Text(
                        text = "Acerque su teléfono al lector para ver y registrar su puntuación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Diálogo de resultados
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = "Resultados del Cuestionario",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Has acertado",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$aciertos de 5",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "preguntas correctamente",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

data class ProgressData(
    val showProgress: Boolean,
    val progress: Float,
    val totalFragmentos: Int,
    val fragmentosRecibidos: Int
)

fun calculateProgress(fase: String): ProgressData {
    // Verificar si debemos mostrar el progress
    val showProgress = fase.contains("QUIZ_INICIADO") || fase.contains("QUIZ_FRAGMENTO")

    if (!showProgress) {
        return ProgressData(false, 0f, 0, 0)
    }

    var totalFragmentos = 0
    var fragmentosRecibidos = 0

    // Extraer información según el formato de la fase
    if (fase.startsWith("QUIZ_INICIADO_")) {
        // Formato: QUIZ_INICIADO_#
        totalFragmentos = fase.substringAfter("QUIZ_INICIADO_").toIntOrNull() ?: 0
        fragmentosRecibidos = 0
    } else if (fase.contains("QUIZ_FRAGMENTO") && fase.contains("_DE") && fase.endsWith("_OK")) {
        // Formato: QUIZ_FRAGMENTO#_DE#_OK
        // Ejemplo: QUIZ_FRAGMENTO1_DE5_OK
        val parts = fase.substringAfter("QUIZ_FRAGMENTO").substringBefore("_OK")
        val numbers = parts.split("_DE")

        if (numbers.size == 2) {
            fragmentosRecibidos = numbers[0].toIntOrNull() ?: 0
            totalFragmentos = numbers[1].toIntOrNull() ?: 0
        }
    }

    // Calcular el progreso
    val progress = if (totalFragmentos > 0) {
        fragmentosRecibidos.toFloat() / totalFragmentos.toFloat()
    } else {
        0f
    }

    return ProgressData(
        showProgress = true,
        progress = progress,
        totalFragmentos = totalFragmentos,
        fragmentosRecibidos = fragmentosRecibidos
    )
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .selectableGroup()
        ) {
            Text(
                text = questionSplited[0],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            radioOptions.forEachIndexed { index, text ->
                Row(
                    Modifier
                        .fillMaxWidth()
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
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (index == optionSelected),
                        onClick = null,
                        enabled = enabled
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}