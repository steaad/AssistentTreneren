package com.example.assistenttreneren.core.auth

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JwtDecoderRoleTest {
    private val decoder = JwtDecoder()

    @Test
    fun `getRole returns administrator from role claim`() {
        assertEquals(UserRole.ADMINISTRATOR, decoder.getRole(jwtWithRole("ADMINISTRATOR")))
    }

    @Test
    fun `getRole returns null for an unknown role`() {
        assertNull(decoder.getRole(jwtWithRole("UNKNOWN")))
    }

    private fun jwtWithRole(role: String): String {
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"role\":\"$role\"}".toByteArray())
        return "header.$payload.signature"
    }
}
