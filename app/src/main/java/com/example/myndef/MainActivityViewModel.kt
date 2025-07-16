package com.example.myndef

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {

    // Estados privados mutables
    private val _actualMessage = MutableStateFlow("No se ha iniciado sesión")
    private val _nameText = MutableStateFlow("")
    private val _phoneNumber = MutableStateFlow("09")
    private val _statusText = MutableStateFlow("")
    private val _requestText = MutableStateFlow("APDU command")
    private val _phoneNumberValid= MutableStateFlow(false)

    // Estados públicos inmutables
    val actualMessage: StateFlow<String> = _actualMessage.asStateFlow()
    val nameText: StateFlow<String> = _nameText.asStateFlow()
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    val statusText: StateFlow<String> = _statusText.asStateFlow()
    val requestText: StateFlow<String> = _requestText.asStateFlow()
    val phoneNumberValid: StateFlow<Boolean> = _phoneNumberValid.asStateFlow()

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

    fun updatePhoneNumber(text: String) {
        _phoneNumber.value = text
    }

    fun updateStatusText(text: String) {
        _statusText.value = text
    }

    fun updateRequestText(text: String) {
        _requestText.value = text
    }

    fun updatePhoneNumberValid(isValid: Boolean){
        _phoneNumberValid.value = isValid
    }

    override fun onCleared() {
        super.onCleared()
        instance = null
    }
}