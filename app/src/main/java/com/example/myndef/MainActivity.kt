package com.example.myndef

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var messageEditText: EditText
    private lateinit var startEmulationButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var requestTextView: TextView

    private val apduReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val command = intent?.getStringExtra("APDU_COMMAND") ?: "Unknown"
            requestTextView.text = command
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        startEmulationButton = findViewById(R.id.startEmulationButton)
        statusTextView = findViewById(R.id.statusTextView)
        requestTextView = findViewById(R.id.requestTextView)

        startEmulationButton.setOnClickListener {
            // Registrar el BroadcastReceiver
            registerReceiver(apduReceiver, IntentFilter("APDU_COMMAND_RECEIVED"), RECEIVER_EXPORTED)

            val message = messageEditText.text.toString()
            if (message.isNotEmpty()) {
                MessageManager.setMessage(message)
                statusTextView.text = "Emulación iniciada. Mensaje cargado para transmitir."
            } else {
                statusTextView.text = "Por favor escriba su mensaje."
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el BroadcastReceiver
        unregisterReceiver(apduReceiver)
    }
}