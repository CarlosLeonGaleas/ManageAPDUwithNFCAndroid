package com.example.myndef

object MessageManager {
    private var message: String = "No hay datos escritos"

    fun setMessage(newMessage: String) {
        message = newMessage
    }

    fun getMessage(): String {
        return message
    }
}