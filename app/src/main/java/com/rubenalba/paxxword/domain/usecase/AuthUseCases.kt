package com.rubenalba.paxxword.domain.usecase

import com.rubenalba.paxxword.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(password: CharArray) = repo.login(password)
}
class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(password: CharArray, defaultEmailLabel: String) = repo.register(password, defaultEmailLabel)
}
class RestoreBackupUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(uriString: String, password: CharArray, defaultEmailLabel: String) = repo.restoreFromBackup(uriString, password, defaultEmailLabel)
}
class ChangeMasterPasswordUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(newPassword: CharArray) = repo.changeMasterPassword(newPassword)
}
class VerifyMasterPasswordUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(password: CharArray) = repo.verifyMasterPassword(password)
}