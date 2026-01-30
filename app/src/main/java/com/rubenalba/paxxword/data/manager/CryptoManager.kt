package com.rubenalba.paxxword.data.manager

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {

    private const val ALGORITHM = "AES/GCM/NoPadding"

    private const val IV_LENGTH = 12

    private const val TAG_LENGTH_BITS = 128

    // create a new random IV for each piece of data stored
    fun generateIv(): ByteArray {
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        return iv
    }

    // encrypt: Text -> Bytes -> Encryption -> Base64
    fun encrypt(plaintext: ByteArray, secretKey: SecretKeySpec, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)

        val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val encryptedBytes = cipher.doFinal(plaintext)

        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    // Decrypt: Base64 -> Bytes -> Decryption -> Text
    fun decrypt(ciphertextBase64: String, secretKey: SecretKeySpec, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

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