package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.util.Locale

object KeyManager {
    private const val PREFS_NAME = "fn_slicer_security_prefs"
    private const val KEY_ACTIVATED = "is_activated"
    private const val KEY_STORED_LICENSE = "stored_license_key"
    private const val KEY_ACTIVATED_DEVICE_ID = "activated_device_id"
    private const val SECRET_SALT = "SHIBLU_HASAN_VIP_FRUIT_NINJA_SLICER_2026_SECURITY_SALT"

    // Master Keys reserved for developer Shiblu Hasan
    private val MASTER_KEYS = setOf(
        "SHIBLU-VIP-2026",
        "SHIBLU-HASAN-DEV-KEY",
        "VIP-FRUIT-NINJA-999",
        "FNSLICE-ADMIN-PRO-2026"
    )

    fun getDeviceId(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
                androidId.uppercase(Locale.ROOT)
            } else {
                val fallback = (Build.BOARD + Build.BRAND + Build.DEVICE + Build.MODEL).hashCode()
                String.format(Locale.ROOT, "%08X%08X", fallback, fallback.inv())
            }
        } catch (e: Exception) {
            "FN" + Math.abs((Build.MODEL + Build.MANUFACTURER).hashCode()).toString(16).uppercase(Locale.ROOT).padStart(12, '0')
        }
    }

    /**
     * Generates a 12-character formatted key: FN-XXXX-XXXX-XXXX bound exclusively to [deviceId]
     */
    fun generateKeyForDevice(deviceId: String): String {
        val raw = "$deviceId:$SECRET_SALT"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02X".format(it) }
        val part1 = hex.substring(0, 4)
        val part2 = hex.substring(4, 8)
        val part3 = hex.substring(8, 12)
        return "FN-$part1-$part2-$part3"
    }

    fun isKeyValid(context: Context, key: String): Boolean {
        val cleanKey = key.trim().uppercase(Locale.ROOT)
        if (cleanKey.isEmpty()) return false

        // Check if master key
        if (MASTER_KEYS.contains(cleanKey)) return true

        // Check if matching current device generated key
        val currentDeviceId = getDeviceId(context)
        val expectedKey = generateKeyForDevice(currentDeviceId)
        return cleanKey == expectedKey || cleanKey == expectedKey.replace("-", "")
    }

    fun activate(context: Context, key: String): Boolean {
        if (isKeyValid(context, key)) {
            val prefs = getPrefs(context)
            prefs.edit()
                .putBoolean(KEY_ACTIVATED, true)
                .putString(KEY_STORED_LICENSE, key.trim().uppercase(Locale.ROOT))
                .putString(KEY_ACTIVATED_DEVICE_ID, getDeviceId(context))
                .apply()
            return true
        }
        return false
    }

    fun isActivated(context: Context): Boolean {
        val prefs = getPrefs(context)
        val isAct = prefs.getBoolean(KEY_ACTIVATED, false)
        val savedDeviceId = prefs.getString(KEY_ACTIVATED_DEVICE_ID, null)
        val currentDeviceId = getDeviceId(context)
        val storedKey = prefs.getString(KEY_STORED_LICENSE, "") ?: ""

        if (!isAct) return false

        // Verify device match to prevent cloning storage
        if (MASTER_KEYS.contains(storedKey)) return true
        return savedDeviceId == currentDeviceId && isKeyValid(context, storedKey)
    }

    fun deactivate(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().clear().apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // =========================================================================
    // DEVELOPER CONTACT CONFIGURATION
    // Set your WhatsApp phone number with country code (e.g., "8801700000000")
    // =========================================================================
    var DEVELOPER_WHATSAPP_NUMBER = "8801724727472" // 👈 এখানে আপনার কান্ট্রি কোডসহ আসল নম্বর দিন
    var DEVELOPER_TELEGRAM_USERNAME = "shibluhasan"

    fun getWhatsAppIntentUrl(deviceId: String): String {
        val message = "Hello Developer Shiblu Hasan, I want to purchase the Fruit Ninja Auto Slicer Activation Key for my Device ID: $deviceId"
        val encoded = java.net.URLEncoder.encode(message, "UTF-8")
        val cleanPhone = DEVELOPER_WHATSAPP_NUMBER.trim().replace("+", "").replace(" ", "").replace("-", "")
        val phoneParam = if (cleanPhone.isNotBlank()) "phone=$cleanPhone&" else ""
        return "https://api.whatsapp.com/send?${phoneParam}text=$encoded"
    }

    fun getTelegramIntentUrl(): String {
        val username = DEVELOPER_TELEGRAM_USERNAME.trim().replace("@", "")
        return if (username.isNotBlank()) "https://t.me/$username" else "https://t.me/shibluhasan"
    }
}
