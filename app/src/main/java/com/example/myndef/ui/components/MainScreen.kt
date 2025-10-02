package com.example.myndef.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.example.myndef.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myndef.MainActivityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colores institucionales
private val InstitutionalBlue = Color(0xFF27348B)
private val InstitutionalOrange = Color(0xFFF3940B)
private val LightGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
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
    val aciertos by viewModel.totalPoints.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }

    val progressData = remember(fase) {
        calculateProgress(fase)
    }

    var showQuestions by remember { mutableStateOf(false) }

    LaunchedEffect(fase, status) {
        if (fase == "QUIZ_RECIBIDO" || status == "CONFIRMED") {
            showQuestions = false
            delay(50)
            showQuestions = true
        }
    }

    LaunchedEffect(fase) {
        if (fase == "QUIZ_CONFIRMED"){
            showDialog = true
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
            val puntos = calcularAciertos()
            viewModel.updateTotalPoints(puntos)

            if (status != "CONFIRMED") {
                MainActivityViewModel.instance?.updateStatusFase("QUIZ_FINISHED")
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
                            modifier = Modifier.width(180.dp)
                        )

                        // Logo derecho (Departamento de Investigación)
                        Image(
                            painter = painterResource(R.drawable.departamentoinv_blanco),
                            contentDescription = "Logo Investigación",
                            modifier = Modifier.height(40.dp)
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            UserInfoCard(name = name, phone = phone, onLogout = onLogout)

            // APDU Card (solo si es necesario mostrarlo)
            /*if (apduCommand.isNotEmpty()) {
                ApduCard(apduCommand)
            }*/

            // Status Message
            if (status == "EMPTY" && !progressData.showProgress) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = InstitutionalBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.contactless_nfc),
                            contentDescription = "NFC",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Encienda el NFC de su teléfono y acérquelo al lector para acceder al cuestionario",
                            style = MaterialTheme.typography.bodyLarge,
                            color = InstitutionalBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Progress Indicator
            if (progressData.showProgress) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Cargando cuestionario...",
                            style = MaterialTheme.typography.titleMedium,
                            color = InstitutionalBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = progressData.progress,
                            modifier = Modifier.fillMaxWidth(),
                            color = InstitutionalOrange,
                            trackColor = Color(0xFFE0E0E0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fragmentos: ${progressData.fragmentosRecibidos}/${progressData.totalFragmentos}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Questions
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
                        questionNumber = 1,
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
                        questionNumber = 2,
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
                        questionNumber = 3,
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
                        questionNumber = 4,
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
                        questionNumber = 5,
                        enabled = status == "COMPLETED",
                        optionSelected = optionQ5Selected,
                        updateSelected = { viewModel.updateQ5Selected(it) }
                    )
                }

                AnimatedVisibility(
                    visible = showQuestions,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 400, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = 400))
                ) {
                    Button(
                        onClick = { onConfirm() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InstitutionalOrange
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.save_responses),
                            contentDescription = "Confirmar",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Confirmar Respuestas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (fase == "QUIZ_FINISHED") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = InstitutionalOrange.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📱 Acerque su teléfono al lector para ver y registrar su puntuación",
                                style = MaterialTheme.typography.bodyLarge,
                                color = InstitutionalOrange,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else if (fase == "QUIZ_CONFIRMED"){
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓ Su puntaje fue: $aciertos. Ya se registró en la Base de Datos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = "🎯 Resultados del Cuestionario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InstitutionalBlue
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Has acertado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    color = InstitutionalOrange.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(60.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$aciertos/5",
                                style = MaterialTheme.typography.displayMedium,
                                color = InstitutionalOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "preguntas correctamente",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = InstitutionalBlue
                        )
                    ) {
                        Text("Aceptar", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
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
    val showProgress = fase.contains("QUIZ_INICIADO") || fase.contains("QUIZ_FRAGMENTO")

    if (!showProgress) {
        return ProgressData(false, 0f, 0, 0)
    }

    var totalFragmentos = 0
    var fragmentosRecibidos = 0

    if (fase.startsWith("QUIZ_INICIADO_")) {
        totalFragmentos = fase.substringAfter("QUIZ_INICIADO_").toIntOrNull() ?: 0
        fragmentosRecibidos = 0
    } else if (fase.contains("QUIZ_FRAGMENTO") && fase.contains("_DE") && fase.endsWith("_OK")) {
        val parts = fase.substringAfter("QUIZ_FRAGMENTO").substringBefore("_OK")
        val numbers = parts.split("_DE")

        if (numbers.size == 2) {
            fragmentosRecibidos = numbers[0].toIntOrNull() ?: 0
            totalFragmentos = numbers[1].toIntOrNull() ?: 0
        }
    }

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
fun UserInfoCard(name: String, phone: String, onLogout: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Información del Usuario",
                style = MaterialTheme.typography.titleMedium,
                color = InstitutionalBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.person_user),
                    contentDescription = "Icono de Usuario",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Nombre",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.mobile_phone),
                    contentDescription = "Icono de Teléfono",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Teléfono",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InstitutionalBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logout),
                        contentDescription = "Cerrar Sesión",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ApduCard(command: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "APDU Command:",
                style = MaterialTheme.typography.labelMedium,
                color = InstitutionalBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = command,
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
fun OptionsQuestion(
    modifier: Modifier = Modifier,
    question: String,
    questionNumber: Int,
    enabled: Boolean = true,
    optionSelected: Int?,
    updateSelected: (Int?) -> Unit
) {
    val questionSplited = question.split("|")
    val radioOptions = listOf(questionSplited[1], questionSplited[2], questionSplited[3], questionSplited[4])

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .selectableGroup()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = InstitutionalBlue,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$questionNumber",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = questionSplited[0],
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            radioOptions.forEachIndexed { index, text ->
                val isSelected = index == optionSelected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) InstitutionalOrange.copy(alpha = 0.1f) else LightGray
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, InstitutionalOrange) else null
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    if (enabled) {
                                        updateSelected(index)
                                    }
                                },
                                role = Role.RadioButton,
                                enabled = enabled
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            enabled = enabled,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = InstitutionalOrange,
                                unselectedColor = Color.Gray
                            )
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp),
                            color = if (isSelected) InstitutionalOrange else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}