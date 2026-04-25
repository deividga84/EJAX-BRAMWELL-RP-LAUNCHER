package com.ejax.bramwell.launcher.ui

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
import com.ejax.bramwell.launcher.utils.VersionManager
import com.ejax.bramwell.launcher.utils.ZipExtractor
import kotlinx.coroutines.launch
import java.io.File

class DownloadActivity : AppCompatActivity() {
    private lateinit var downloadHelper: DownloadHelper
    private lateinit var configManager: ConfigManager
    private lateinit var versionManager: VersionManager
    private lateinit var zipExtractor: ZipExtractor

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var statusText: TextView
    private lateinit var cancelButton: Button

    private var dataUrl = ""
    private var dataVersion = ""
    private var isCancelled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        // Inicializar managers
        downloadHelper = DownloadHelper(this)
        configManager = ConfigManager(this)
        versionManager = VersionManager(this)
        zipExtractor = ZipExtractor()

        // Inicializar views
        progressBar = findViewById(R.id.downloadProgress)
        progressText = findViewById(R.id.progressText)
        statusText = findViewById(R.id.statusText)
        cancelButton = findViewById(R.id.cancelButton)

        cancelButton.setOnClickListener {
            isCancelled = true
            finish()
        }

        // Obter dados da intent
        dataUrl = intent.getStringExtra("dataUrl") ?: ""
        dataVersion = intent.getStringExtra("dataVersion") ?: ""

        if (dataUrl.isEmpty()) {
            showError("URL de download não fornecida")
            return
        }

        // Iniciar download
        startDownload()
    }

    private fun startDownload() {
        statusText.text = "Obtendo informações do arquivo..."
        
        val downloadFile = File(getExternalFilesDir(null), "data.zip")

        lifecycleScope.launch {
            downloadHelper.downloadFile(dataUrl, downloadFile, object : DownloadHelper.DownloadListener {
                override fun onProgress(current: Long, total: Long) {
                    if (isCancelled) return

                    val percentage = if (total > 0) {
                        ((current * 100) / total).toInt()
                    } else {
                        0
                    }

                    runOnUiThread {
                        progressBar.progress = percentage
                        val mb = current / (1024 * 1024)
                        val totalMb = total / (1024 * 1024)
                        progressText.text = "$mb MB / $totalMb MB"
                        statusText.text = "Baixando... $percentage%"
                    }
                }

                override fun onSuccess(file: File) {
                    if (isCancelled) {
                        file.delete()
                        return
                    }

                    runOnUiThread {
                        statusText.text = "Download concluído! Extraindo arquivos..."
                        progressBar.progress = 0
                        startExtraction(file)
                    }
                }

                override fun onError(message: String) {
                    runOnUiThread {
                        showError("Erro no download: $message")
                    }
                }
            })
        }
    }

    private fun startExtraction(zipFile: File) {
        val targetDir = File(getExternalFilesDir(null), "data")

        // Limpar diretório anterior
        if (targetDir.exists()) {
            zipExtractor.deleteDirectory(targetDir)
        }

        zipExtractor.extractZip(zipFile, targetDir, object : ZipExtractor.ExtractionListener {
            override fun onProgress(current: Int, total: Int) {
                if (isCancelled) return

                val percentage = if (total > 0) {
                    ((current * 100) / total)
                } else {
                    0
                }

                runOnUiThread {
                    progressBar.progress = percentage
                    progressText.text = "$current / $total arquivos"
                    statusText.text = "Extraindo... $percentage%"
                }
            }

            override fun onSuccess() {
                if (isCancelled) return

                runOnUiThread {
                    progressBar.progress = 100
                    statusText.text = "Extração concluída!"
                    
                    // Salvar versão
                    versionManager.saveVersion(dataVersion)
                    
                    // Salvar no ConfigManager
                    lifecycleScope.launch {
                        configManager.saveInstalledDataVersion(dataVersion)
                        configManager.saveDataFolderPath(targetDir.absolutePath)
                        
                        Toast.makeText(
                            this@DownloadActivity,
                            "Dados atualizados com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Limpar arquivo ZIP
                        zipFile.delete()
                        
                        // Retornar para MainActivity
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }

            override fun onError(message: String) {
                if (!isCancelled) {
                    runOnUiThread {
                        showError("Erro na extração: $message")
                    }
                }
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
