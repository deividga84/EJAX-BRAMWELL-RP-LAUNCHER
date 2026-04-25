package com.ejax.bramwell.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferences
import androidx.datastore.preferences.core.stringSetPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val CONFIG_STORE = "config_datastore"

val Context.configDataStore: DataStore<Preferences> by preferencesDataStore(name = CONFIG_STORE)

class ConfigManager(private val context: Context) {
    companion object {
        private const val SERVER_NAME = "server_name"
        private const val SERVER_IP = "server_ip"
        private const val SERVER_PORT = "server_port"
        private const val DATA_VERSION = "data_version"
        private const val DATA_URL = "data_url"
        private const val LAUNCHER_VERSION = "launcher_version"
        private const val LAUNCHER_APK_URL = "launcher_apk_url"
        private const val MAINTENANCE = "maintenance"
        private const val MAINTENANCE_MESSAGE = "maintenance_message"
        private const val DISCORD_URL = "discord_url"
        private const val WHATSAPP_URL = "whatsapp_url"
        private const val INSTALLED_DATA_VERSION = "installed_data_version"
        private const val DATA_FOLDER_PATH = "data_folder_path"
    }

    suspend fun saveServerName(name: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(SERVER_NAME)] = name
        }
    }

    fun getServerName(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(SERVER_NAME)] ?: "EJAX BRAMWELL RP"
    }

    suspend fun saveServerIp(ip: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(SERVER_IP)] = ip
        }
    }

    fun getServerIp(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(SERVER_IP)] ?: "181.215.45.38"
    }

    suspend fun saveServerPort(port: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(SERVER_PORT)] = port
        }
    }

    fun getServerPort(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(SERVER_PORT)] ?: "7788"
    }

    suspend fun saveDataVersion(version: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(DATA_VERSION)] = version
        }
    }

    fun getDataVersion(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(DATA_VERSION)] ?: "0.0.0"
    }

    suspend fun saveDataUrl(url: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(DATA_URL)] = url
        }
    }

    fun getDataUrl(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(DATA_URL)] ?: ""
    }

    suspend fun saveLauncherVersion(version: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(LAUNCHER_VERSION)] = version
        }
    }

    fun getLauncherVersion(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(LAUNCHER_VERSION)] ?: "1.0.0"
    }

    suspend fun saveLauncherApkUrl(url: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(LAUNCHER_APK_URL)] = url
        }
    }

    fun getLauncherApkUrl(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(LAUNCHER_APK_URL)] ?: ""
    }

    suspend fun saveMaintenance(maintenance: Boolean) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(MAINTENANCE)] = maintenance.toString()
        }
    }

    fun getMaintenance(): Flow<Boolean> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(MAINTENANCE)]?.toBoolean() ?: false
    }

    suspend fun saveMaintenanceMessage(message: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(MAINTENANCE_MESSAGE)] = message
        }
    }

    fun getMaintenanceMessage(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(MAINTENANCE_MESSAGE)] ?: ""
    }

    suspend fun saveDiscordUrl(url: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(DISCORD_URL)] = url
        }
    }

    fun getDiscordUrl(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(DISCORD_URL)] ?: ""
    }

    suspend fun saveWhatsappUrl(url: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(WHATSAPP_URL)] = url
        }
    }

    fun getWhatsappUrl(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(WHATSAPP_URL)] ?: ""
    }

    suspend fun saveInstalledDataVersion(version: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(INSTALLED_DATA_VERSION)] = version
        }
    }

    fun getInstalledDataVersion(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(INSTALLED_DATA_VERSION)] ?: "0.0.0"
    }

    suspend fun saveDataFolderPath(path: String) {
        context.configDataStore.edit { preferences ->
            preferences[stringPreferences(DATA_FOLDER_PATH)] = path
        }
    }

    fun getDataFolderPath(): Flow<String> = context.configDataStore.data.map { preferences ->
        preferences[stringPreferences(DATA_FOLDER_PATH)] ?: ""
    }
}

// Extensão para facilitar o uso de edit
suspend inline fun <reified T : Preferences> DataStore<T>.edit(
    block: suspend (MutablePreferences) -> Unit
) {
    updateData { preferences ->
        preferences.toMutablePreferences().apply {
            @Suppress("UNCHECKED_CAST")
            block(this as MutablePreferences)
        }
    }
}
