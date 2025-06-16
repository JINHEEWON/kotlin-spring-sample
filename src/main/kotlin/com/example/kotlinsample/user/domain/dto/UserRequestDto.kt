package com.example.kotlinsample.user.domain.dto

import com.example.kotlinsample.user.domain.model.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserUpdateRequest(
    @field:Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    val name: String?,

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String?
)

data class PasswordChangeRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다")
    val newPassword: String
)

data class UserRoleUpdateRequest(
    val role: UserRole
)

data class UserStatusUpdateRequest(
    val active: Boolean
)