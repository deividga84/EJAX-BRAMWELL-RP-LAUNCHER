package com.ejax.bramwell.launcher.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ejax.bramwell.launcher.R
import com.ejax.bramwell.launcher.data.ConfigManager
import com.ejax.bramwell.launcher.network.DownloadHelper
import com.ejax.bramwell.launcher.utils.GameLauncherHelper
import com.ejax.bramwell.launcher.utils.PermissionHelper
import com.ejax.bramwell.launcher.utils.VersionManager
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var configManager: ConfigManager
    private lateinit var downloadHelper: DownloadHelper
    private lateinit var gameHelper: GameLauncherHelper
    private lateinit var versionManager: VersionManager
    private lateinit var permissionHelper: PermissionHelper

    private lateinit var serverNameText: TextView
    private lateinit var serverIpText: TextView
    private lateinit var serverPortText: TextView
    private lateinit var installedVersionText: TextView
    private lateinit var onlineVersionText: TextView
    private lateinit var updateButton: Button
    private lateinit var chooseFolderButton: Button
    private lateinit var playButton: Button
    private lateinit var settingsButton: Button
    private lateinit var loadingProgress: ProgressBar

    private var currentConfig: com.ejax.bramwell.launcher.network.ServerConfig? = null
    private var configUrl = "https://SEU-LINK-AQUI.com/config.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar managers
        configManager = ConfigManager(this)
        downloadHelper = DownloadHelper(this)
        gameHelper = GameLauncherHelper(this)
        versionManager = VersionManager(this)
        permissionHelper = PermissionHelper(this)

        // Inicializar views
        initializeViews()

        // Verificar permissões
        if (!permissionHelper.hasStoragePermissions()) {
            requestPermissions(permissionHelper.getRequiredPermissions(), PERMISSION_REQUEST_CODE)
        }

        // Carregar configuração
        loadConfiguration()
        loadStoredData()
    }

    private fun initializeViews() {
        serverNameText = findViewById(R.id.serverNameText)
        serverIpText = findViewById(R.id.serverIpText)
        serverPortText = findViewById(R.id.serverPortText)
        installedVersionText = findViewById(R.id.installedVersionText)
        onlineVersionText = findViewById(R.id.onlineVersionText)
        updateButton = findViewById(R.id.updateButton)
        chooseFolderButton = findViewById(R.id.chooseFolderButton)
        playButton = findViewById(R.id.playButton)
        settingsButton = findViewById(R.id.settingsButton)
        loadingProgress = findViewById(R.id.loadingProgress)

        updateButton.setOnClickListener { downloadUpdate() }
        chooseFolderButton.setOnClickListener { chooseFolder() }
        playButton.setOnClickListener { launchGame() }
        settingsButton.setOnClickListener { openSettings() }
    }

    private fun loadStoredData() {
        lifecycleScope.launch {
            configManager.getServerName().collect { name ->
                serverNameText.text = name
            }
        }

        lifecycleScope.launch {
            configManager.getServerIp().collect { ip ->
                serverIpText.text = ip
            }
        }

        lifecycleScope.launch {
            configManager.getServerPort().collect { port ->
                serverPortText.text = port
            }
        }

        lifecycleScope.launch {
            configManager.getInstalledDataVersion().collect { version ->
                installedVersionText.text = "Versão instalada: $version"
            }
        }
    }

    private fun loadConfiguration() {
        loadingProgress.visibility = View.VISIBLE
        updateButton.isEnabled = false

        lifecycleScope.launch {
            // Tentar carregar de um arquivo local primeiro
            val localConfigFile = File(cacheDir, "config.json")
            
            downloadHelper.downloadConfig(configUrl, object : DownloadHelper.DownloadListener {
                override fun onProgress(current: Long, total: Long) {}

                override fun onSuccess(file: File) {
                    val config = downloadHelper.parseConfig(file)
                    if (config != null) {
                        currentConfig = config
                        updateUIWithConfig(config)
                        saveConfigData(config)
                    } else {
                        showError("Configuração inválida")
                    }
                    loadingProgress.visibility = View.GONE
                    updateButton.isEnabled = true
                }

                override fun onError(message: String) {
                    showError("Erro ao carregar config: $message")
                    loadingProgress.visibility = View.GONE
                    updateButton.isEnabled = true
                }
            })
        }
    }

    private fun saveConfigData(config: com.ejax.bramwell.launcher.network.ServerConfig) {
        lifecycleScope.launch {
            configManager.saveServerName(config.server_name)
            configManager.saveServerIp(config.server_ip)
            configManager.saveServerPort(config.server_port)
            configManager.saveDataVersion(config.data_version)
            configManager.saveDataUrl(config.data_url)
            configManager.saveLauncherVersion(config.launcher_version)
            configManager.saveLauncherApkUrl(config.launcher_apk_url)
            configManager.saveMaintenance(config.maintenance)
            configManager.saveMaintenanceMessage(config.maintenance_message)
            configManager.saveDiscordUrl(config.discord_url)
            configManager.saveWhatsappUrl(config.whatsapp_url)
        }
    }

    private fun updateUIWithConfig(config: com.ejax.bramwell.launcher.network.ServerConfig) {
        serverNameText.text = config.server_name
        serverIpText.text = config.server_ip
        serverPortText.text = config.server_port
        onlineVersionText.text = "Versão online: ${config.data_version}"

        if (config.maintenance) {
            Toast.makeText(this, config.maintenance_message, Toast.LENGTH_LONG).show()
            updateButton.isEnabled = false
        }
    }

    private fun downloadUpdate() {
        if (currentConfig == null) {
            showError("Configuração não carregada")
            return
        }

        val intent = Intent(this, DownloadActivity::class.java)
        intent.putExtra("dataUrl", currentConfig!!.data_url)
        intent.putExtra("dataVersion", currentConfig!!.data_version)
        startActivityForResult(intent, DOWNLOAD_REQUEST_CODE)
    }

    private fun chooseFolder() {
        Toast.makeText(this, "Pasta padrão: /sdcard/EJAX", Toast.LENGTH_SHORT).show()
    }

    private fun launchGame() {
        val serverIp = serverIpText.text.toString()
        val serverPort = serverPortText.text.toString()

        val success = gameHelper.launchGame(serverIp, serverPort)

        if (!success) {
            // Mostrar dados para copiar manualmente
            showGameNotInstalledDialog(serverIp, serverPort)
        }
    }

    private fun showGameNotInstalledDialog(serverIp: String, serverPort: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Configurações de Conexão")
        builder.setMessage("IP: $serverIp\nPorta: $serverPort\n\nCopie essas informações no aplicativo SAMP Mobile")
        builder.setPositiveButton("Baixar SAMP Mobile") { _, _ ->
            gameHelper.openPlayStore("com.rockstargames.gta.sa")
        }
        builder.setNegativeButton("Fechar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DOWNLOAD_REQUEST_CODE && resultCode == RESULT_OK) {
            // Atualizar versão instalada
            loadStoredData()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val DOWNLOAD_REQUEST_CODE = 101
    }
}
