package com.ejax.bramwell.launcher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

class GameLauncherHelper(private val context: Context) {
    companion object {
        private const val SAMP_PACKAGE = "com.rockstargames.gta.sa"
        private const val GTA_MOBILE_PACKAGE = "com.rockstargames.gta"
        private const val ALTERNATIVE_SAMP_PACKAGE = "com.sam.samp"
    }

    fun launchGame(serverIp: String, serverPort: String): Boolean {
        val packages = listOf(SAMP_PACKAGE, GTA_MOBILE_PACKAGE, ALTERNATIVE_SAMP_PACKAGE)

        for (packageName in packages) {
            try {
                if (isAppInstalled(packageName)) {
                    return launchAppWithServer(packageName, serverIp, serverPort)
                }
            } catch (e: Exception) {
                continue
            }
        }

        return false
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun launchAppWithServer(packageName: String, serverIp: String, serverPort: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false

            // Tentar passar como dados
            intent.data = Uri.parse("samp://$serverIp:$serverPort")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                // Se falhar, tenta abrir sem parâmetros
                val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun getInstalledSampApp(): String {
        return when {
            isAppInstalled(SAMP_PACKAGE) -> SAMP_PACKAGE
            isAppInstalled(GTA_MOBILE_PACKAGE) -> GTA_MOBILE_PACKAGE
            isAppInstalled(ALTERNATIVE_SAMP_PACKAGE) -> ALTERNATIVE_SAMP_PACKAGE
            else -> ""
        }
    }

    fun openPlayStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
