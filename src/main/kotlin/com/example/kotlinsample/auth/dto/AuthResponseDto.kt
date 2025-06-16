package com.example.kotlinsample.auth.dto

import com.example.kotlinsample.user.domain.model.UserRole
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class LoginResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @JsonProperty("expires_in")
    val expiresIn: Long,

    val user: UserInfo
) {
    data class UserInfo(
        val id: Long,
        val email: String,
        val name: String,
        val role: UserRole,
        val createdAt: LocalDateTime
    )
}

data class RegisterResponse(
    val user: UserInfo,
    val message: String = "회원가입이 완료되었습니다"
) {
    data class UserInfo(
        val id: Long,
        val email: String,
        val name: String,
        val role: UserRole,
        val createdAt: LocalDateTime
    )
}