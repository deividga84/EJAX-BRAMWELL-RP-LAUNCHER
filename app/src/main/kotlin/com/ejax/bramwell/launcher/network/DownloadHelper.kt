package com.ejax.bramwell.launcher.network

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

data class ServerConfig(
    val server_name: String,
    val server_ip: String,
    val server_port: String,
    val server_address: String,
    val data_version: String,
    val data_url: String,
    val launcher_version: String,
    val launcher_apk_url: String,
    val maintenance: Boolean,
    val maintenance_message: String,
    val discord_url: String,
    val whatsapp_url: String
)

class DownloadHelper(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()

    interface DownloadListener {
        fun onProgress(current: Long, total: Long)
        fun onSuccess(file: File)
        fun onError(message: String)
    }

    fun downloadConfig(url: String, listener: DownloadListener) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                listener.onError("Erro ao baixar configuração: ${response.code}")
                return
            }

            val configJson = response.body?.string()
            if (configJson.isNullOrEmpty()) {
                listener.onError("Configuração vazia")
                return
            }

            val configFile = File(context.cacheDir, "config.json")
            configFile.writeText(configJson)
            listener.onSuccess(configFile)
        } catch (e: Exception) {
            listener.onError("Erro: ${e.message}")
        }
    }

    fun parseConfig(file: File): ServerConfig? {
        return try {
            val json = file.readText()
            gson.fromJson(json, ServerConfig::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun downloadFile(url: String, outputFile: File, listener: DownloadListener) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                listener.onError("Erro ao baixar arquivo: ${response.code}")
                return
            }

            val body = response.body ?: run {
                listener.onError("Resposta vazia")
                return
            }

            val totalSize = body.contentLength()
            var downloadedSize = 0L

            FileOutputStream(outputFile).use { out ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        listener.onProgress(downloadedSize, totalSize)
                    }
                }
            }

            listener.onSuccess(outputFile)
        } catch (e: Exception) {
            listener.onError("Erro: ${e.message}")
            outputFile.delete()
        }
    }

    fun getFileSizeFromUrl(url: String): Long {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val response = client.newCall(request).execute()
            response.header("Content-Length")?.toLongOrNull() ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }
}
