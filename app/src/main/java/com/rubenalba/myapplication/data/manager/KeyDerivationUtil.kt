package com.rubenalba.myapplication.data.manager

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object KeyDerivationUtil {

    private const val ITERATIONS = 600000 // Repeat the process 10k times to make it "slow" and safe
    private const val KEY_LENGTH = 256   // Standard military security size (AES-256)
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16) // 16 bytes is the standard
        random.nextBytes(salt)
        return salt
    }

    fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)

        try {
            val keyBytes = factory.generateSecret(spec).encoded
            return SecretKeySpec(keyBytes, "AES")
        } finally {
            // erase password memory copy of PBEKeySpec
            spec.clearPassword()
        }
    }
}