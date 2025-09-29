package com.example.myndef.ui.components

import androidx.lifecycle.ViewModel
import com.example.myndef.MainActivityViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainScreenViewModel : ViewModel(){
    // Estados privados mutables
    private val _status = MutableStateFlow("EMPTY")
    private val _q1 = MutableStateFlow("")
    private val _q1Selected = MutableStateFlow<Int?>(null)
    private val _q2 = MutableStateFlow("")
    private val _q2Selected = MutableStateFlow<Int?>(null)
    private val _q3 = MutableStateFlow("")
    private val _q3Selected = MutableStateFlow<Int?>(null)
    private val _q4 = MutableStateFlow("")
    private val _q4Selected = MutableStateFlow<Int?>(null)
    private val _q5 = MutableStateFlow("")
    private val _q5Selected = MutableStateFlow<Int?>(null)

    // Estados públicos inmutables
    val status: StateFlow<String> = _status.asStateFlow()
    val q1: StateFlow<String> = _q1.asStateFlow()
    val q1Selected: StateFlow<Int?> = _q1Selected.asStateFlow()
    val q2: StateFlow<String> = _q2.asStateFlow()
    val q2Selected: StateFlow<Int?> = _q2Selected.asStateFlow()
    val q3: StateFlow<String> = _q3.asStateFlow()
    val q3Selected: StateFlow<Int?> = _q3Selected.asStateFlow()
    val q4: StateFlow<String> = _q4.asStateFlow()
    val q4Selected: StateFlow<Int?> = _q4Selected.asStateFlow()
    val q5: StateFlow<String> = _q5.asStateFlow()
    val q5Selected: StateFlow<Int?> = _q5Selected.asStateFlow()

    // Instancia estática para acceso desde el BroadcastReceiver
    companion object {
        var instance: MainScreenViewModel? = null
    }

    init {
        instance = this
    }

    fun updateStatus(status: String){
        _status.value = status
    }

    fun updateQ1(question: String){
        _q1.value = question
    }
    fun updateQ1Selected(selected: Int?){
        _q1Selected.value = selected
    }

    fun updateQ2(question: String){
        _q2.value = question
    }
    fun updateQ2Selected(selected: Int?){
        _q2Selected.value = selected
    }

    fun updateQ3(question: String){
        _q3.value = question
    }
    fun updateQ3Selected(selected: Int?){
        _q3Selected.value = selected
    }

    fun updateQ4(question: String){
        _q4.value = question
    }
    fun updateQ4Selected(selected: Int?){
        _q4Selected.value = selected
    }

    fun updateQ5(question: String){
        _q5.value = question
    }
    fun updateQ5Selected(selected: Int?){
        _q5Selected.value = selected
    }
}