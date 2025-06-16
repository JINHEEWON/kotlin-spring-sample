package com.example.kotlinsample.post.domain.dto

import com.example.kotlinsample.post.domain.model.PostStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PostRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(max = 255, message = "제목은 255자 이하여야 합니다")
    val title: String,

    val content: String?,

    val status: PostStatus = PostStatus.DRAFT,

    val fileIds: List<Long>? = null
)

data class PostUpdateRequest(
    @field:Size(max = 255, message = "제목은 255자 이하여야 합니다")
    val title: String?,

    val content: String?,

    val status: PostStatus?,

    val fileIds: List<Long>? = null
)