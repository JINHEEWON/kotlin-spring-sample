package com.example.kotlinsample.file.domain.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class FileUploadInitiateRequest(
    @field:NotBlank(message = "파일명은 필수입니다")
    val fileName: String,

    @field:NotNull(message = "파일 크기는 필수입니다")
    @field:Positive(message = "파일 크기는 0보다 커야 합니다")
    val fileSize: Long,

    val contentType: String?
)

data class ChunkUploadRequest(
    @field:NotBlank(message = "업로드 ID는 필수입니다")
    val uploadId: String,

    @field:NotNull(message = "청크 번호는 필수입니다")
    val chunkNumber: Int
)

data class CompleteUploadRequest(
    @field:NotBlank(message = "업로드 ID는 필수입니다")
    val uploadId: String
)