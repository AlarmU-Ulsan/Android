package com.uou.alarmit

import android.net.Uri

object UrlSecurity {
    private val allowedHosts = setOf(
        "alarmitu.app",
        "alarm-it.ulsan.ac.kr",
        "ulsan.ac.kr",
        "www.ulsan.ac.kr",
        "play.google.com",
        "apps.apple.com"
    )

    fun normalizeSafeUrl(url: String?): String? {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        val uri = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase() ?: return null
        val host = uri.host?.lowercase() ?: return null

        if (!uri.isHierarchical || scheme != "https") return null
        if (allowedHosts.none { host == it || host.endsWith(".$it") }) return null

        return uri.toString()
    }
}
