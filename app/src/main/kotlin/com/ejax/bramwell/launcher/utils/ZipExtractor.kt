package com.ejax.bramwell.launcher.utils

import android.content.Context
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipExtractor {
    interface ExtractionListener {
        fun onProgress(current: Int, total: Int)
        fun onSuccess()
        fun onError(message: String)
    }

    fun extractZip(zipFile: File, targetDir: File, listener: ExtractionListener) {
        try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            ZipInputStream(zipFile.inputStream()).use { zipInput ->
                var entry: ZipEntry? = zipInput.nextEntry

                // Contar total de arquivos
                val totalEntries = mutableListOf<ZipEntry>()
                ZipInputStream(zipFile.inputStream()).use { counter ->
                    while (counter.nextEntry.also { entry = it } != null) {
                        totalEntries.add(entry!!)
                    }
                }

                val total = totalEntries.size
                var current = 0

                // Extrair
                ZipInputStream(zipFile.inputStream()).use { extractor ->
                    while (extractor.nextEntry.also { entry = it } != null) {
                        val entryFile = File(targetDir, entry!!.name)

                        if (entry!!.isDirectory) {
                            entryFile.mkdirs()
                        } else {
                            entryFile.parentFile?.mkdirs()
                            entryFile.outputStream().use { out ->
                                extractor.copyTo(out)
                            }
                        }

                        current++
                        listener.onProgress(current, total)
                    }
                }
            }

            listener.onSuccess()
        } catch (e: Exception) {
            listener.onError("Erro na extração: ${e.message}")
        }
    }

    fun deleteDirectory(dir: File) {
        if (dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
        }
        dir.delete()
    }
}
