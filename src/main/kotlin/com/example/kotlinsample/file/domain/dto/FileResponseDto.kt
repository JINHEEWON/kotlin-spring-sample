package com.example.kotlinsample.file.domain.dto

import com.example.kotlinsample.file.domain.model.UploadStatus
import java.time.LocalDateTime

data class FileUploadInitiateResponse(
    val uploadId: String,
    val chunkSize: Long,
    val totalChunks: Int
)

data class ChunkUploadResponse(
    val chunkNumber: Int,
    val uploaded: Boolean,
    val message: String? = null
)

data class FileUploadCompleteResponse(
    val fileId: Long,
    val originalName: String,
    val storedName: String,
    val fileSize: Long,
    val downloadUrl: String
)

data class FileDetailResponse(
    val id: Long,
    val originalName: String,
    val storedName: String,
    val fileSize: Long,
    val contentType: String?,
    val uploadStatus: UploadStatus,
    val uploader: UploaderResponse,
    val downloadUrl: String,
    val thumbnailUrl: String?,
    val createdAt: LocalDateTime
)

data class UploaderResponse(
    val id: Long,
    val name: String
)

data class FileListResponse(
    val id: Long,
    val originalName: String,
    val fileSize: Long,
    val contentType: String?,
    val uploadStatus: UploadStatus,
    val createdAt: LocalDateTime,
    val downloadUrl: String
)