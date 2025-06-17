package com.example.kotlinsample.post.domain.dto

import com.example.kotlinsample.post.domain.model.PostStatus
import org.springframework.data.domain.Page
import java.time.LocalDateTime

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String?,
    val status: PostStatus,
    val author: AuthorResponse,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val files: List<FileResponse>? = null
)

data class AuthorResponse(
    val email: String,  // id 대신 email 사용
    val name: String
)

data class PostListResponse(
    val posts: Page<PostSummaryResponse>
)

data class PostSummaryResponse(
    val id: Long,
    val title: String,
    val content: String?,
    val status: PostStatus,
    val author: AuthorResponse,
    val createdAt: LocalDateTime,
    val fileCount: Int
)

data class FileResponse(
    val id: Long,
    val originalName: String,
    val fileSize: Long,
    val contentType: String?,
    val downloadUrl: String,
    val createdAt: LocalDateTime
)