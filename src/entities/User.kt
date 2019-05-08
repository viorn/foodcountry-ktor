package com.taganhorn.entities

import com.google.gson.annotations.Expose
import com.taganhorn.security.Role

data class User(
    @Expose val id: Int = -1,
    @Expose val name: String,
    val password: String,
    @Expose val roles: List<Role>
)