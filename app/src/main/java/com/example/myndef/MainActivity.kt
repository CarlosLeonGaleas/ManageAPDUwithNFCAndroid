package com.example.myndef

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var actualMessageTextView: TextView
    private lateinit var namesEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var startEmulationButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var requestTextView: TextView

    private val apduReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val command = intent?.getStringExtra("APDU_COMMAND") ?: "Unknown"
            requestTextView.text = command
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar MessageManager con el contexto de la actividad
        MessageManager.initialize(this)

        actualMessageTextView = findViewById(R.id.actualMessageTextView)
        namesEditText = findViewById(R.id.messageEditText)
        phoneEditText = findViewById(R.id.numberPhoneText)
        startEmulationButton = findViewById(R.id.startEmulationButton)
        statusTextView = findViewById(R.id.statusTextView)
        requestTextView = findViewById(R.id.requestTextView)

        actualMessageTextView.text = MessageManager.getMessage()

        // Registrar el BroadcastReceiver
        registerReceiver(apduReceiver, IntentFilter("APDU_COMMAND_RECEIVED"), RECEIVER_EXPORTED)

        startEmulationButton.setOnClickListener {

            val name = namesEditText.text.toString()
            val phoneNumber = phoneEditText.text.toString()
            if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                MessageManager.setMessage(name+phoneNumber)
                statusTextView.text = "Acerque su télefono al lector NFC"
            } else {
                statusTextView.text = "Por favor ingrese todos los datos para el iniciar el registro"
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el BroadcastReceiver
        unregisterReceiver(apduReceiver)
    }
}