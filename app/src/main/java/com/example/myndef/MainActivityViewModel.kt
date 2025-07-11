package com.example.myndef

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {

    // Estados privados mutables
    private val _actualMessage = MutableStateFlow("No hay datos escritos")
    private val _nameText = MutableStateFlow("")
    private val _phoneText = MutableStateFlow("")
    private val _statusText = MutableStateFlow("")
    private val _requestText = MutableStateFlow("APDU command")

    // Estados públicos inmutables
    val actualMessage: StateFlow<String> = _actualMessage.asStateFlow()
    val nameText: StateFlow<String> = _nameText.asStateFlow()
    val phoneText: StateFlow<String> = _phoneText.asStateFlow()
    val statusText: StateFlow<String> = _statusText.asStateFlow()
    val requestText: StateFlow<String> = _requestText.asStateFlow()

    // Instancia estática para acceso desde el BroadcastReceiver
    companion object {
        var instance: MainActivityViewModel? = null
    }

    init {
        instance = this
    }

    // Funciones para actualizar los estados
    fun updateActualMessage(message: String) {
        _actualMessage.value = message
    }

    fun updateNameText(text: String) {
        _nameText.value = text
    }

    fun updatePhoneText(text: String) {
        _phoneText.value = text
    }

    fun updateStatusText(text: String) {
        _statusText.value = text
    }

    fun updateRequestText(text: String) {
        _requestText.value = text
    }

    override fun onCleared() {
        super.onCleared()
        instance = null
    }
}