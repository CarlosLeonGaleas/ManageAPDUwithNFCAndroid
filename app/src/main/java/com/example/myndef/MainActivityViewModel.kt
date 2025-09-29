package com.example.myndef

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {

    // Estados privados mutables
    private val _lastLogin = MutableStateFlow("CLOSED_SESSION")
    private val _isLogged = MutableStateFlow(false)
    private val _nameText = MutableStateFlow("")
    private val _phoneNumber = MutableStateFlow("09")
    private val _statusFase = MutableStateFlow("INITIAL_LOGIN")
    private val _requestText = MutableStateFlow("APDU command")
    private val _phoneNumberValid= MutableStateFlow(false)

    // Estados públicos inmutables
    val lastLogin: StateFlow<String> = _lastLogin.asStateFlow()
    val isLogged: StateFlow<Boolean> = _isLogged.asStateFlow()
    val nameText: StateFlow<String> = _nameText.asStateFlow()
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    val statusFase: StateFlow<String> = _statusFase.asStateFlow()
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
    fun updateLastLogin(message: String) {
        _lastLogin.value = message
    }

    fun updateIsLogged(isLogged: Boolean){
        _isLogged.value = isLogged
    }

    fun updateNameText(text: String) {
        _nameText.value = text
    }

    fun updatePhoneNumber(text: String) {
        _phoneNumber.value = text
    }

    fun updateStatusFase(text: String) {
        _statusFase.value = text
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