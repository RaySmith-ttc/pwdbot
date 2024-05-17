package ru.raysmith.pwdbot.utils

import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Blowfish {
    fun encrypt(password: String, key: String): ByteArray {
        val spec = SecretKeySpec(key.toByteArray(), "Blowfish")
        val cipher = Cipher.getInstance("Blowfish")
        cipher.init(Cipher.ENCRYPT_MODE, spec)
        return cipher.doFinal(password.toByteArray(charset("UTF-8")))
    }
    
    fun decrypt(data: ByteArray, key: String): String {
        val spec = SecretKeySpec(key.toByteArray(), "Blowfish")
        val cipher = Cipher.getInstance("Blowfish")
        cipher.init(Cipher.DECRYPT_MODE, spec)
        val decrypted = cipher.doFinal(data)
        return String(decrypted, Charset.forName("UTF-8"))
    }
}

