package com.example.myndef.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.example.myndef.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    name: String,
    phone: String,
    apduCommand: String,
    onLogout: () -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {
    // Estados observables del ViewModel
    val q1 by viewModel.q1.collectAsState()
    val q2 by viewModel.q2.collectAsState()
    val q3 by viewModel.q3.collectAsState()
    val q4 by viewModel.q4.collectAsState()
    val q5 by viewModel.q5.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserCard(name = name, phone = phone, onLogout = onLogout)

        Text("Acerque su teléfono para acceder al cuestionario")

        ApduCard(apduCommand)

        OptionsQuestion(question = q1)
        OptionsQuestion(question = q2)
        OptionsQuestion(question = q3)
        OptionsQuestion(question = q4)
        OptionsQuestion(question = q5)
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
) {
    val questionSplited = question.split("|")
    val radioOptions = listOf(questionSplited[1], questionSplited[2], questionSplited[3], questionSplited[4])
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(modifier.selectableGroup()) {
        Text(questionSplited[0])
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screen readers
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


