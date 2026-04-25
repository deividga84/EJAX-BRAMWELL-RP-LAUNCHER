package com.ejax.bramwell.launcher.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ejax.bramwell.launcher.R
import com.ejax.bramwell.launcher.data.ConfigManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var configManager: ConfigManager

    private lateinit var serverUrlInput: EditText
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        configManager = ConfigManager(this)

        // Inicializar views
        serverUrlInput = findViewById(R.id.serverUrlInput)
        saveButton = findViewById(R.id.saveButton)
        resetButton = findViewById(R.id.resetButton)

        saveButton.setOnClickListener { saveSettings() }
        resetButton.setOnClickListener { resetSettings() }

        loadCurrentSettings()
    }

    private fun loadCurrentSettings() {
        lifecycleScope.launch {
            configManager.getServerIp().collect { ip ->
                serverUrlInput.setText(ip)
            }
        }
    }

    private fun saveSettings() {
        val newUrl = serverUrlInput.text.toString().trim()

        if (newUrl.isEmpty()) {
            Toast.makeText(this, "Insira um valor válido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            configManager.saveServerIp(newUrl)
            Toast.makeText(this@SettingsActivity, "Configurações salvas!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun resetSettings() {
        lifecycleScope.launch {
            configManager.saveServerIp("181.215.45.38")
            serverUrlInput.setText("181.215.45.38")
            Toast.makeText(this@SettingsActivity, "Configurações resetadas!", Toast.LENGTH_SHORT).show()
        }
    }
}
