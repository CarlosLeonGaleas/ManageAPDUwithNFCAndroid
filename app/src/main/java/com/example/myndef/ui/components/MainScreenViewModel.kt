package com.example.myndef.ui.components

import androidx.lifecycle.ViewModel
import com.example.myndef.MainActivityViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainScreenViewModel : ViewModel(){
    // Estados privados mutables
    private val _q1 = MutableStateFlow("")
    private val _q2 = MutableStateFlow("¿Cuál es la capital del Ecuador?|Quito|Ambato|Guayaquil|Cuenca|2")
    private val _q3 = MutableStateFlow("")
    private val _q4 = MutableStateFlow("¿Cuál es la capital del Ecuador?|Cuenca|Quito|Guayaquil|Ambato|2")
    private val _q5 = MutableStateFlow("")

    // Estados públicos inmutables
    val q1: StateFlow<String> = _q1.asStateFlow()
    val q2: StateFlow<String> = _q2.asStateFlow()
    val q3: StateFlow<String> = _q3.asStateFlow()
    val q4: StateFlow<String> = _q4.asStateFlow()
    val q5: StateFlow<String> = _q5.asStateFlow()

    // Instancia estática para acceso desde el BroadcastReceiver
    companion object {
        var instance: MainScreenViewModel? = null
    }

    init {
        instance = this
    }

    fun updateQ1(question: String){
        _q1.value = question
    }

    fun updateQ2(question: String){
        _q2.value = question
    }

    fun updateQ3(question: String){
        _q3.value = question
    }

    fun updateQ4(question: String){
        _q4.value = question
    }

    fun updateQ5(question: String){
        _q5.value = question
    }
}