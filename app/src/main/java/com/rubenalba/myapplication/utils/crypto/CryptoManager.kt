package com.rubenalba.myapplication.utils.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    // create a new random IV for each piece of data stored
    fun generateIv(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

    // encrypt: Text -> Bytes -> Encryption -> Base64
    fun encrypt(plaintext: String, secretKey: SecretKeySpec, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    // Decrypt: Base64 -> Bytes -> Decryption -> Text
    fun decrypt(ciphertextBase64: String, secretKey: SecretKeySpec, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        val decodedBytes = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun base64ToBytes(base64: String): ByteArray {
        return Base64.decode(base64, Base64.NO_WRAP)
    }

    fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}