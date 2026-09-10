package com.example.assistenttreneren.core.auth

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.contentOrNull

@Singleton
class JwtDecoder @Inject constructor() {
    fun isExpired(token: String, currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val expiresAtSeconds = getExpiresAtSeconds(token) ?: return true
        val currentTimeSeconds = currentTimeMillis / MILLIS_PER_SECOND
        return expiresAtSeconds <= currentTimeSeconds
    }

    fun getRole(token: String): UserRole? {
        val payload = token.split(".").getOrNull(PAYLOAD_INDEX) ?: return null
        val decodedPayload = decodePayload(payload) ?: return null
        val jsonObject = runCatching {
            Json.parseToJsonElement(decodedPayload).jsonObject
        }.getOrNull() ?: return null

        return when (jsonObject[ROLE_CLAIM]?.jsonPrimitive?.contentOrNull) {
            UserRole.ADMINISTRATOR.name -> UserRole.ADMINISTRATOR
            UserRole.TRAINER.name -> UserRole.TRAINER
            else -> null
        }
    }

    private fun getExpiresAtSeconds(token: String): Long? {
        val payload = token.split(".").getOrNull(PAYLOAD_INDEX) ?: return null
        val decodedPayload = decodePayload(payload) ?: return null
        val jsonObject = runCatching {
            Json.parseToJsonElement(decodedPayload).jsonObject
        }.getOrNull() ?: return null

        return jsonObject[EXPIRES_AT_CLAIM]?.jsonPrimitive?.longOrNull
    }

    private fun decodePayload(payload: String): String? =
        runCatching {
            val decodedBytes = Base64.getUrlDecoder().decode(payload)
            String(decodedBytes, StandardCharsets.UTF_8)
        }.getOrNull()

    private companion object {
        const val PAYLOAD_INDEX = 1
        const val EXPIRES_AT_CLAIM = "exp"
        const val ROLE_CLAIM = "role"
        const val MILLIS_PER_SECOND = 1_000L
    }
}
