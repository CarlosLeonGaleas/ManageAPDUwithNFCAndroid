package com.example.myndef

import android.content.Context
import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log

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

        // Protocolo híbrido
        private const val MAGIC_BYTE_1: Byte = 0xAA.toByte()
        private const val MAGIC_BYTE_2: Byte = 0x55.toByte()
        private const val PROTOCOL_VERSION: Byte = 0x01

        // Tipos de mensajes
        private const val MSG_INIT_QUIZ: Byte = 0x01
        private const val MSG_FRAGMENT: Byte = 0x02
        private const val MSG_END_TRANSFER: Byte = 0x03
        private const val MSG_READY: Byte = 0x10
        private const val MSG_ACK_FRAGMENT: Byte = 0x11
        private const val MSG_NACK_FRAGMENT: Byte = 0x12
        private const val MSG_QUIZ_COMPLETE: Byte = 0x13
        private const val MSG_ERROR: Byte = 0xFF.toByte()

        // Comandos APDU personalizados
        private const val GET_DATA_INS = 0xCA.toByte()
        private const val PUT_DATA_INS = 0xDA.toByte()
        private const val GET_CHALLENGE_INS = 0x84.toByte()
        private const val READ_BINARY_INS = 0xB0.toByte()
        private const val UPDATE_BINARY_INS = 0xD6.toByte()

        // Comandos específicos de la aplicación
        private const val CMD_UPDATE_Q1 = "UPDATE_Q1"
        private const val CMD_UPDATE_Q2 = "UPDATE_Q2"
        private const val CMD_UPDATE_Q3 = "UPDATE_Q3"
        private const val CMD_UPDATE_Q4 = "UPDATE_Q4"
        private const val CMD_UPDATE_Q5 = "UPDATE_Q5"
        private const val CMD_GET_USER_DATA = "GET_USER_DATA"
        private const val CMD_GET_BATTERY = "GET_BATTERY"
        private const val CMD_GET_LOCATION = "GET_LOCATION"
        private const val CMD_TAKE_PHOTO = "TAKE_PHOTO"
        private const val CMD_SEND_SMS = "SEND_SMS"
        private const val CMD_SET_CONFIG = "SET_CONFIG"
    }

    // Estado del protocolo
    private var transferState = TransferState.IDLE
    private var expectedFragments = 0
    private var expectedTotalSize = 0
    private val receivedFragments = mutableMapOf<Int, ByteArray>()
    private val fragmentStatus = mutableMapOf<Int, Boolean>()
    private var quizDataBuffer: ByteArray? = null

    enum class TransferState {
        IDLE,
        READY,
        RECEIVING_FRAGMENTS,
        PROCESSING_QUIZ,
        COMPLETE,
        ERROR
    }

    data class FragmentHeader(
        val magicByte1: Byte,
        val magicByte2: Byte,
        val version: Byte,
        val messageType: Byte,
        val totalSize: Int,
        val totalFragments: Int,
        val currentFragment: Int,
        val fragmentSize: Int,
        val checksum: Int
    )

    // Recibe TODOS los comandos del ESP, los identifica y decide qué hacer con cada uno
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        return when {
            // SELECT AID - Seleccionar aplicación
            commandApdu.contentEquals(SELECT_APDU) -> {
                val isLoggedIn = MainActivityViewModel.instance?.isLogged?.value
                var responseMessage = "App NFC Conectada"
                if (isLoggedIn == true){
                    responseMessage = responseMessage + " - ${MessageManager.getMessage()}"
                }
                else{
                    responseMessage = "$responseMessage - CLOSED_SESSION"
                }
                createResponse(responseMessage.toByteArray())
            }

            // GET DATA - Obtener datos del teléfono
            commandApdu.size >= 2 && commandApdu[1] == GET_DATA_INS -> {
                handleGetData(commandApdu)
            }

            // PUT DATA - Recibir comandos del ESP8266
            commandApdu.size >= 5 && commandApdu[1] == PUT_DATA_INS -> {
                handleProtocolMessage(commandApdu)
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
        val p2 = if (commandApdu.size > 3) commandApdu[3].toInt() and 0xFF else 0

        val response = when (p1) {
            0x10 -> { // Get Status - para READY
                when (transferState) {
                    TransferState.READY -> "READY"
                    TransferState.RECEIVING_FRAGMENTS -> "RECEIVING"
                    TransferState.PROCESSING_QUIZ -> "PROCESSING"
                    TransferState.COMPLETE -> "COMPLETE"
                    TransferState.ERROR -> "ERROR"
                    else -> "IDLE"
                }
            }
            0x11 -> { // Get Fragment ACK
                val fragmentIndex = p2
                if (fragmentStatus[fragmentIndex] == true) {
                    "ACK_$fragmentIndex"
                } else {
                    "NACK_$fragmentIndex"
                }
            }
            0x13 -> { // Get Quiz Complete Status
                when (transferState) {
                    TransferState.COMPLETE -> "QUIZ_COMPLETE"
                    TransferState.ERROR -> "QUIZ_ERROR"
                    else -> "QUIZ_PENDING"
                }
            }
            else -> {
                "UNKNOWN_REQUEST"
            }
        }

        return createResponse(response.toByteArray())
    }

    private fun handleProtocolMessage(commandApdu: ByteArray): ByteArray {
        if (commandApdu.size < 5) return ERROR_SW

        val dataLength = commandApdu[4].toInt() and 0xFF
        if (commandApdu.size < 5 + dataLength) return ERROR_SW

        val data = commandApdu.copyOfRange(5, 5 + dataLength)

        // Verificar que tenemos al menos el header mínimo
        if (data.size < 12) return ERROR_SW

        val header = parseFragmentHeader(data)
        if (!isValidHeader(header)) return ERROR_SW

        return when (header.messageType) {
            MSG_INIT_QUIZ -> handleInitQuiz(header)
            MSG_FRAGMENT -> handleFragment(header, data)
            MSG_END_TRANSFER -> handleEndTransfer(header)
            else -> {
                broadcastCommand("Tipo de mensaje desconocido: ${header.messageType}")
                ERROR_SW
            }
        }
    }

    private fun parseFragmentHeader(data: ByteArray): FragmentHeader {
        // Debug: Mostrar bytes del header
        Log.d("APDU", "Header bytes: ${data.take(12).joinToString(" ") { "%02X".format(it) }}")

        return FragmentHeader(
            magicByte1 = data[0],
            magicByte2 = data[1],
            version = data[2],
            messageType = data[3],
            // Cambiar orden: Little Endian (LSB primero)
            totalSize = (data[4].toInt() and 0xFF) or ((data[5].toInt() and 0xFF) shl 8),
            totalFragments = data[6].toInt() and 0xFF,
            currentFragment = data[7].toInt() and 0xFF,
            // Cambiar orden: Little Endian (LSB primero)
            fragmentSize = (data[8].toInt() and 0xFF) or ((data[9].toInt() and 0xFF) shl 8),
            checksum = (data[10].toInt() and 0xFF) or ((data[11].toInt() and 0xFF) shl 8)
        )
    }

    private fun isValidHeader(header: FragmentHeader): Boolean {
        return header.magicByte1 == MAGIC_BYTE_1 &&
                header.magicByte2 == MAGIC_BYTE_2 &&
                header.version == PROTOCOL_VERSION
    }

    private fun handleInitQuiz(header: FragmentHeader): ByteArray {
        Log.d("APDU", "Iniciando transferencia de quiz: ${header.totalSize} bytes, ${header.totalFragments} fragmentos")

        // Resetear estado
        resetTransferState()

        // Configurar transferencia
        expectedFragments = header.totalFragments
        expectedTotalSize = header.totalSize
        transferState = TransferState.READY

        // Inicializar buffers
        receivedFragments.clear()
        fragmentStatus.clear()
        for (i in 0 until expectedFragments) {
            fragmentStatus[i] = false
        }

        broadcastCommand("INIT_QUIZ:Iniciando recepción de $expectedFragments fragmentos")
        if (MainActivityViewModel.instance?.statusFase?.value == "QUIZ_RECIBIDO"){
            return createResponse("QUIZ_RECIBIDO".toByteArray())
        } else if (MainActivityViewModel.instance?.statusFase?.value == "QUIZ_CONFIRMED"){
            return createResponse("QUIZ_CONFIRMED".toByteArray())
        }
        else{
            broadcastCommand("UPDATE_FASE:QUIZ_INICIADO_$expectedFragments")
            return createResponse("INIT_OK".toByteArray())
        }
    }

    private fun handleFragment(header: FragmentHeader, fullData: ByteArray): ByteArray {
        Log.d("APDU", "Recibiendo fragmento ${header.currentFragment}/${header.totalFragments}")
        Log.d("APDU", "Header info - fragmentSize: ${header.fragmentSize}, fullData.size: ${fullData.size}")

        if (transferState != TransferState.READY && transferState != TransferState.RECEIVING_FRAGMENTS) {
            broadcastCommand("ERROR:Estado inválido para recibir fragmento")
            return ERROR_SW
        }

        // Validar fragmento
        if (header.currentFragment >= expectedFragments) {
            broadcastCommand("ERROR:Número de fragmento inválido")
            return ERROR_SW
        }

        // Extraer datos del fragmento (después del header)
        val headerSize = 12
        val expectedDataSize = headerSize + header.fragmentSize

        Log.d("APDU", "Validación: headerSize=$headerSize, fragmentSize=${header.fragmentSize}, expectedDataSize=$expectedDataSize, fullData.size=${fullData.size}")

        if (fullData.size < expectedDataSize) {
            broadcastCommand("ERROR:Tamaño de fragmento inválido - esperado: $expectedDataSize, recibido: ${fullData.size}")
            return ERROR_SW
        }

        // Verificar que header.fragmentSize sea razonable (máximo 100 según tu configuración)
        if (header.fragmentSize > 100 || header.fragmentSize < 0) {
            broadcastCommand("ERROR:Tamaño de fragmento fuera de rango: ${header.fragmentSize}")
            return ERROR_SW
        }

        val fragmentData = fullData.copyOfRange(headerSize, headerSize + header.fragmentSize)

        // Verificar checksum
        val calculatedChecksum = calculateChecksum(fragmentData)
        if (calculatedChecksum != header.checksum) {
            Log.w("APDU", "Checksum inválido para fragmento ${header.currentFragment}")
            Log.w("APDU", "Calculado: $calculatedChecksum, Esperado: ${header.checksum}")
            broadcastCommand("ERROR:Checksum inválido fragmento ${header.currentFragment}")
            return ERROR_SW
        }

        // Almacenar fragmento
        receivedFragments[header.currentFragment] = fragmentData
        fragmentStatus[header.currentFragment] = true
        transferState = TransferState.RECEIVING_FRAGMENTS

        Log.d("APDU", "Fragmento ${header.currentFragment} almacenado correctamente (${fragmentData.size} bytes)")
        broadcastCommand("UPDATE_FASE:QUIZ_FRAGMENTO${header.currentFragment + 1}_DE${expectedFragments}_OK")
        broadcastCommand("FRAGMENT_OK:Fragmento ${header.currentFragment + 1}/${expectedFragments} recibido")

        return createResponse("FRAG_OK".toByteArray())
    }

    private fun handleEndTransfer(header: FragmentHeader): ByteArray {
        Log.d("APDU", "Finalizando transferencia")

        if (transferState != TransferState.RECEIVING_FRAGMENTS) {
            broadcastCommand("ERROR:Estado inválido para finalizar transferencia")
            return ERROR_SW
        }

        // Verificar que todos los fragmentos fueron recibidos
        val allReceived = fragmentStatus.all { it.value }
        if (!allReceived) {
            val missingFragments = fragmentStatus.filterNot { it.value }.keys
            broadcastCommand("ERROR:Fragmentos faltantes: $missingFragments")
            transferState = TransferState.ERROR
            return ERROR_SW
        }

        // Reconstruir datos completos
        if (reconstructQuizData()) {
            transferState = TransferState.PROCESSING_QUIZ
            processQuizData()
            broadcastCommand("TRANSFER_COMPLETE:Quiz procesado exitosamente")
            broadcastCommand("STATUS_DATA:COMPLETED")
            broadcastCommand("UPDATE_FASE:QUIZ_RECIBIDO")
            return createResponse("END_OK".toByteArray())
        } else {
            transferState = TransferState.ERROR
            broadcastCommand("ERROR:Error al reconstruir datos del quiz")
            return ERROR_SW
        }
    }

    private fun reconstructQuizData(): Boolean {
        try {
            val totalSize = receivedFragments.values.sumOf { it.size }
            if (totalSize != expectedTotalSize) {
                Log.e("APDU", "Tamaño total no coincide: esperado $expectedTotalSize, recibido $totalSize")
                return false
            }

            quizDataBuffer = ByteArray(expectedTotalSize)
            var offset = 0

            // Ordenar fragmentos y concatenar
            for (i in 0 until expectedFragments) {
                val fragment = receivedFragments[i] ?: return false
                System.arraycopy(fragment, 0, quizDataBuffer, offset, fragment.size)
                offset += fragment.size
            }

            Log.d("APDU", "Datos del quiz reconstruidos exitosamente: $totalSize bytes")
            return true

        } catch (e: Exception) {
            Log.e("APDU", "Error al reconstruir datos: ${e.message}")
            return false
        }
    }

    private fun processQuizData(): Boolean {
        val buffer = quizDataBuffer ?: return false

        try {
            var offset = 0

            // Leer número de preguntas
            val numQuestions = buffer[offset].toInt() and 0xFF
            offset++

            Log.d("APDU", "Procesando $numQuestions preguntas")

            val questions = mutableListOf<QuizQuestion>()

            for (i in 0 until numQuestions) {
                // Leer tamaño de pregunta
                val questionLen = ((buffer[offset].toInt() and 0xFF) shl 8) or (buffer[offset + 1].toInt() and 0xFF)
                offset += 2

                // Leer pregunta
                val questionText = String(buffer, offset, questionLen, Charsets.UTF_8)
                offset += questionLen

                // Leer opciones
                val options = mutableListOf<String>()
                for (j in 0 until 4) {
                    val optionLen = ((buffer[offset].toInt() and 0xFF) shl 8) or (buffer[offset + 1].toInt() and 0xFF)
                    offset += 2

                    val optionText = String(buffer, offset, optionLen, Charsets.UTF_8)
                    offset += optionLen
                    options.add(optionText)
                }

                // Leer índice respuesta correcta
                val correctIndex = buffer[offset].toInt() and 0xFF
                offset++

                questions.add(QuizQuestion(questionText, options, correctIndex))

                // Broadcast individual para cada pregunta
                broadcastCommand("UPDATE_Q${i + 1}:$questionText|${options.joinToString("|")}|$correctIndex")
            }

            transferState = TransferState.COMPLETE
            broadcastCommand("QUIZ_PROCESSED:${questions.size} preguntas procesadas exitosamente")
            return true

        } catch (e: Exception) {
            Log.e("APDU", "Error al procesar quiz: ${e.message}")
            transferState = TransferState.ERROR
            return false
        }
    }

    private fun calculateChecksum(data: ByteArray): Int {
        var checksum = 0
        for (byte in data) {
            checksum += (byte.toInt() and 0xFF)
        }
        return checksum and 0xFFFF
    }

    private fun resetTransferState() {
        transferState = TransferState.IDLE
        expectedFragments = 0
        expectedTotalSize = 0
        receivedFragments.clear()
        fragmentStatus.clear()
        quizDataBuffer = null
    }

    private fun handlePutData(commandApdu: ByteArray): ByteArray {
        val dataLength = commandApdu[4].toInt() and 0xFF
        if (commandApdu.size >= 5 + dataLength) {
            val data = commandApdu.copyOfRange(5, 5 + dataLength)
            val command = String(data)

            val response = when {
                command.startsWith(CMD_UPDATE_Q1) -> {
                    val value = command.substringAfter(":")
                    broadcastCommand("UPDATE_Q1:$value")
                    "Q1 actualizado: $value"
                }
                command.startsWith(CMD_UPDATE_Q2) -> {
                    val value = command.substringAfter(":")
                    broadcastCommand("UPDATE_Q2:$value")
                    "Q2 actualizado: $value"
                }
                command.startsWith(CMD_UPDATE_Q3) -> {
                    val value = command.substringAfter(":")
                    broadcastCommand("UPDATE_Q3:$value")
                    "Q3 actualizado: $value"
                }
                command.startsWith(CMD_UPDATE_Q4) -> {
                    val value = command.substringAfter(":")
                    broadcastCommand("UPDATE_Q4:$value")
                    "Q4 actualizado: $value"
                }
                command.startsWith(CMD_UPDATE_Q5) -> {
                    val value = command.substringAfter(":")
                    broadcastCommand("UPDATE_Q5:$value")
                    "Q5 actualizado: $value"
                }
                else -> processCustomCommand(command)
                //CMD_GET_USER_DATA -> getUserData()
                //CMD_GET_BATTERY -> getBatteryLevel()
                //CMD_GET_LOCATION -> getLocation()
                //CMD_TAKE_PHOTO -> takePhoto()
                //CMD_SEND_SMS -> sendSMS()
                //CMD_SET_CONFIG -> setConfiguration(command)
                //else -> processCustomCommand(command)
            }

            //broadcastCommand("PUT_DATA: $command")
            return createResponse(SUCCESS_SW)
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
            command.startsWith("ECHO:") -> {
                val message = command.substring(5)
                broadcastCommand("UPDATE_STATUS:Echo recibido: $message")
                message
            }
            command.equals("PING") -> {
                broadcastCommand("UPDATE_STATUS:Ping recibido")
                "PONG"
            }
            command.equals("TIME") -> {
                val time = System.currentTimeMillis().toString()
                broadcastCommand("UPDATE_STATUS:Tiempo solicitado")
                time
            }
            // Nuevos comandos personalizados
            command.startsWith("SET_Q1:") -> {
                val value = command.substringAfter("SET_Q1:")
                broadcastCommand("UPDATE_Q1:$value")
                "Q1 configurado: $value"
            }
            command.startsWith("SET_Q2:") -> {
                val value = command.substringAfter("SET_Q2:")
                broadcastCommand("UPDATE_Q2:$value")
                "Q2 configurado: $value"
            }
            command.startsWith("SET_Q3:") -> {
                val value = command.substringAfter("SET_Q3:")
                broadcastCommand("UPDATE_Q3:$value")
                "Q3 configurado: $value"
            }
            command.startsWith("SET_Q4:") -> {
                val value = command.substringAfter("SET_Q4:")
                broadcastCommand("UPDATE_Q4:$value")
                "Q4 configurado: $value"
            }
            command.startsWith("SET_Q5:") -> {
                val value = command.substringAfter("SET_Q5:")
                broadcastCommand("UPDATE_Q5:$value")
                "Q5 configurado: $value"
            }
            else -> {
                broadcastCommand("UPDATE_STATUS:Comando desconocido: $command")
                "Comando desconocido: $command"
            }
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

    fun broadcastCommand(command: String) {
        val intent = Intent("APDU_COMMAND_RECEIVED").apply {
            putExtra("APDU_COMMAND", command)
        }
        sendBroadcast(intent)
        Log.d("APDU", "Broadcast: $command")
    }

    override fun onDeactivated(reason: Int) {
        Log.d("APDU", "NFC desactivado - Razón: $reason")
        resetTransferState()
        broadcastCommand("NFC_DISCONNECTED:Razón $reason")
    }

    // Clase auxiliar para las preguntas
    data class QuizQuestion(
        val text: String,
        val options: List<String>,
        val correctIndex: Int
    )
}