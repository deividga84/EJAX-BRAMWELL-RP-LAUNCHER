package com.ejax.bramwell.launcher.utils

import android.content.Context

class VersionManager(private val context: Context) {
    private val fileName = "${context.filesDir.absolutePath}/version.txt"

    fun getInstalledVersion(): String {
        return try {
            java.io.File(fileName).readText().trim()
        } catch (e: Exception) {
            "0.0.0"
        }
    }

    fun saveVersion(version: String) {
        try {
            java.io.File(fileName).writeText(version)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isUpdateAvailable(onlineVersion: String): Boolean {
        val installed = getInstalledVersion()
        return compareVersions(onlineVersion, installed) > 0
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(parts1.size, parts2.size)
        val v1 = parts1.toMutableList()
        val v2 = parts2.toMutableList()

        while (v1.size < maxLength) v1.add(0)
        while (v2.size < maxLength) v2.add(0)

        for (i in 0 until maxLength) {
            if (v1[i] > v2[i]) return 1
            if (v1[i] < v2[i]) return -1
        }

        return 0
    }
}
