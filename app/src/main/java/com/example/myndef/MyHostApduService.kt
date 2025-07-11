package com.example.myndef

import android.content.Context
import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.BatteryManager
import android.os.Build
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
        private val ERROR_SW = byteArrayOf(0x6A.toByte(), 0x82.toByte()) // File not found

        // Comandos APDU personalizados
        private const val GET_DATA_INS = 0xCA.toByte()
        private const val PUT_DATA_INS = 0xDA.toByte()
        private const val GET_CHALLENGE_INS = 0x84.toByte()
        private const val READ_BINARY_INS = 0xB0.toByte()
        private const val UPDATE_BINARY_INS = 0xD6.toByte()

        // Comandos específicos de la aplicación
        private const val CMD_GET_USER_DATA = "GET_USER_DATA"
        private const val CMD_GET_BATTERY = "GET_BATTERY"
        private const val CMD_GET_LOCATION = "GET_LOCATION"
        private const val CMD_TAKE_PHOTO = "TAKE_PHOTO"
        private const val CMD_SEND_SMS = "SEND_SMS"
        private const val CMD_SET_CONFIG = "SET_CONFIG"
    }

    // Recibe TODOS los comandos del ESP, los identifica y decide qué hacer con cada uno
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        return when {
            // SELECT AID - Seleccionar aplicación
            commandApdu.contentEquals(SELECT_APDU) -> {
                val responseMessage = "App NFC Conectada - ${MessageManager.getMessage()}"
                createResponse(responseMessage.toByteArray())
            }

            // GET DATA - Obtener datos del teléfono
            commandApdu.size >= 2 && commandApdu[1] == GET_DATA_INS -> {
                handleGetData(commandApdu)
            }

            // PUT DATA - Recibir comandos del ESP8266
            commandApdu.size >= 5 && commandApdu[1] == PUT_DATA_INS -> {
                handlePutData(commandApdu)
            }

            // GET CHALLENGE - Generar desafío de seguridad
            commandApdu.size >= 2 && commandApdu[1] == GET_CHALLENGE_INS -> {
                val challenge = generateChallenge()
                createResponse(challenge)
            }

            // READ BINARY - Leer datos binarios
            commandApdu.size >= 2 && commandApdu[1] == READ_BINARY_INS -> {
                handleReadBinary(commandApdu)
            }

            // UPDATE BINARY - Actualizar datos
            commandApdu.size >= 5 && commandApdu[1] == UPDATE_BINARY_INS -> {
                handleUpdateBinary(commandApdu)
            }

            // Comando desconocido
            else -> {
                val commandHex = commandApdu.joinToString(" ") { String.format("%02X", it) }
                broadcastCommand(commandHex)
                ERROR_SW
            }
        }
    }

    private fun handleGetData(commandApdu: ByteArray): ByteArray {
        val p1 = commandApdu[2].toInt() and 0xFF
        val p2 = commandApdu[3].toInt() and 0xFF

        val data = when (p1) {
            0x01 -> getUserData()
            0x02 -> getBatteryLevel()
            0x03 -> getDeviceInfo()
            0x04 -> getLocation()
            else -> MessageManager.getMessage()
        }

        return createResponse(data.toByteArray())
    }

    private fun handlePutData(commandApdu: ByteArray): ByteArray {
        val dataLength = commandApdu[4].toInt() and 0xFF
        if (commandApdu.size >= 5 + dataLength) {
            val data = commandApdu.copyOfRange(5, 5 + dataLength)
            val command = String(data)

            val response = when (command) {
                CMD_GET_USER_DATA -> getUserData()
                CMD_GET_BATTERY -> getBatteryLevel()
                CMD_GET_LOCATION -> getLocation()
                CMD_TAKE_PHOTO -> takePhoto()
                CMD_SEND_SMS -> sendSMS()
                CMD_SET_CONFIG -> setConfiguration(command)
                else -> processCustomCommand(command)
            }

            broadcastCommand("PUT_DATA: $command")
            return createResponse(response.toByteArray())
        }

        return ERROR_SW
    }

    private fun handleReadBinary(commandApdu: ByteArray): ByteArray {
        val offset = ((commandApdu[2].toInt() and 0xFF) shl 8) or (commandApdu[3].toInt() and 0xFF)
        val length = if (commandApdu.size > 4) commandApdu[4].toInt() and 0xFF else 0

        val data = MessageManager.getMessage()
        val dataBytes = data.toByteArray()

        return if (offset < dataBytes.size) {
            val endIndex = minOf(offset + length, dataBytes.size)
            val result = dataBytes.copyOfRange(offset, endIndex)
            createResponse(result)
        } else {
            ERROR_SW
        }
    }

    private fun handleUpdateBinary(commandApdu: ByteArray): ByteArray {
        val dataLength = commandApdu[4].toInt() and 0xFF
        if (commandApdu.size >= 5 + dataLength) {
            val data = commandApdu.copyOfRange(5, 5 + dataLength)
            val newMessage = String(data)
            MessageManager.setMessage(newMessage)

            broadcastCommand("UPDATE_BINARY: $newMessage")
            return SUCCESS_SW
        }

        return ERROR_SW
    }

    private fun getUserData(): String {
        return "Usuario: ${MessageManager.getMessage()}"
    }

    private fun getBatteryLevel(): String {
        return try {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            "Batería: $batteryLevel%"
        } catch (e: Exception) {
            "Batería: No disponible"
        }
    }

    private fun getDeviceInfo(): String {
        return "Dispositivo: ${Build.MODEL} - Android ${Build.VERSION.RELEASE}"
    }

    private fun getLocation(): String {
        // Implementar obtención de ubicación si es necesario
        return "Ubicación: No disponible"
    }

    private fun takePhoto(): String {
        // Implementar captura de foto si es necesario
        return "Foto: Función no implementada"
    }

    private fun sendSMS(): String {
        // Implementar envío de SMS si es necesario
        return "SMS: Función no implementada"
    }

    private fun setConfiguration(config: String): String {
        // Implementar configuración si es necesario
        return "Config: Guardada"
    }

    private fun processCustomCommand(command: String): String {
        // Procesar comandos personalizados
        return when {
            command.startsWith("ECHO:") -> command.substring(5)
            command.equals("PING") -> "PONG"
            command.equals("TIME") -> System.currentTimeMillis().toString()
            else -> "Comando desconocido: $command"
        }
    }

    private fun generateChallenge(): ByteArray {
        val challenge = ByteArray(8)
        val random = java.security.SecureRandom()
        random.nextBytes(challenge)
        return challenge
    }

    private fun createResponse(data: ByteArray): ByteArray {
        val response = ByteArray(data.size + 2)
        System.arraycopy(data, 0, response, 0, data.size)
        System.arraycopy(SUCCESS_SW, 0, response, data.size, 2)
        return response
    }

    private fun broadcastCommand(command: String) {
        val intent = Intent("APDU_COMMAND_RECEIVED").apply {
            putExtra("APDU_COMMAND", command)
        }
        sendBroadcast(intent)
    }

    override fun onDeactivated(reason: Int) {
        broadcastCommand("NFC Desconectado - Razón: $reason")
    }
}