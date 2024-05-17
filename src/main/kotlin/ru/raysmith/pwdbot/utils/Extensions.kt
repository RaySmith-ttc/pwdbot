package ru.raysmith.pwdbot.utils

import ru.raysmith.pwdbot.database.Location
import ru.raysmith.tgbot.core.LocationHandler
import ru.raysmith.tgbot.utils.locations.LocationConfig
import ru.raysmith.tgbot.utils.locations.LocationsWrapper
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun LocationHandler<*>.toLocation(location: Location) = toLocation(location.name)
fun <T : LocationConfig> LocationsWrapper<T>.location(
    location: Location, newLocation: ru.raysmith.tgbot.utils.locations.Location<T>.() -> Unit
) = location(location.name, newLocation)

fun String.toHex(salt: String): String {
    val key = SecretKeySpec(salt.toByteArray(), "Blowfish")
    val cipher = Cipher.getInstance("Blowfish")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(this.toByteArray()).toHex()
}

fun String.toReal(salt: String): String {
    val key = SecretKeySpec(salt.toByteArray(), "Blowfish")
    val cipher = Cipher.getInstance("Blowfish")
    cipher.init(Cipher.DECRYPT_MODE, key)
    return String(cipher.doFinal(this.decodeHex()))
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }
    
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}