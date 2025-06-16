package com.example.kotlinsample.user.domain.dto

import com.example.kotlinsample.user.domain.model.UserRole
import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class UserDetailResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isActive: Boolean = true,
    val postCount: Long = 0,
    val fileCount: Long = 0
)

data class UserListResponse(
    val users: Page<UserSummaryResponse>
)

data class UserSummaryResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val isActive: Boolean = true
)

data class ProfileResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val statistics: UserStatistics
)

data class UserStatistics(
    val postCount: Long,
    val publishedPostCount: Long,
    val draftPostCount: Long,
    val fileCount: Long,
    val totalFileSize: Long
)