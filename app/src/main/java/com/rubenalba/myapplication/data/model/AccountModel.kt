package com.rubenalba.myapplication.data.model

data class AccountModel(
    val id: Long = 0,
    val folderId: Long? = null,
    val serviceName: String,
    val username: String,
    val password: String,
    val url: String = "",
    val notes: String = ""
)