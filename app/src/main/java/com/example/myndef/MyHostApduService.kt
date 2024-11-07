package com.example.myndef

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class MyHostApduService : HostApduService() {
    companion object {
        private val SELECT_APDU = byteArrayOf(
            0x00, // CLA
            0xA4.toByte(), // INS
            0x04, // P1
            0x00, // P2
            0x07, // Lc
            0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // AID
            0x00 // LE
        )
        private val SUCCESS_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())
        //private val SUCCESS_SW = "Hello From DEFECTO".toByteArray()
        private const val GET_DATA_APDU = 0xCA.toByte()
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        return when {
            //commandApdu.contentEquals(SELECT_APDU) -> SUCCESS_SW
            commandApdu.contentEquals(SELECT_APDU) -> {
                //val responseMessage = "Hello From Android".toByteArray()
                //val response = ByteArray(responseMessage.size + 2)
                val responseMessage = MessageManager.getMessage().toByteArray()
                val response = ByteArray(responseMessage.size+2)
                System.arraycopy(responseMessage, 0, response, 0, responseMessage.size)
                System.arraycopy(SUCCESS_SW, 0, response, responseMessage.size, 2)
                response
            }

            commandApdu.isNotEmpty() && commandApdu[0] == GET_DATA_APDU -> {
                val message = MessageManager.getMessage()
                val messageBytes = message.toByteArray()
                val response = ByteArray(messageBytes.size + 2)
                System.arraycopy(messageBytes, 0, response, 0, messageBytes.size)
                System.arraycopy(SUCCESS_SW, 0, response, messageBytes.size, 2)
                response
            }

            //else -> SUCCESS_SW
            else -> {
                // Convertir el APDU a una cadena en hexadecimal
                val commandHex = commandApdu.joinToString(" ") { String.format("%02X", it) }

                // Enviar el comando recibido a la Activity usando un Broadcast
                val intent = Intent("APDU_COMMAND_RECEIVED").apply {
                    putExtra("APDU_COMMAND", commandHex)
                }
                sendBroadcast(intent)

                // Responder con el código de estado de éxito
                SUCCESS_SW
            }
        }
    }

    override fun onDeactivated(reason: Int) {
        // Se llama cuando la conexión NFC termina
    }
}