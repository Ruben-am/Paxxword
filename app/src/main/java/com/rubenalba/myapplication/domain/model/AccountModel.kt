package com.rubenalba.myapplication.domain.model

data class AccountModel(
    val id: Long = 0,
    val folderId: Long? = null,
    val serviceName: String,

    val username: String = "",
    val email: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = ""
)