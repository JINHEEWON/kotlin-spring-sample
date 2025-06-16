package com.example.kotlinsample.file.domain.model

import com.example.kotlinsample.post.domain.model.Post
import com.example.kotlinsample.user.domain.model.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "files")
@EntityListeners(AuditingEntityListener::class)
data class File(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val originalName: String,

    @Column(nullable = false)
    val storedName: String,

    @Column(nullable = false, length = 500)
    val filePath: String,

    @Column(nullable = false)
    val fileSize: Long,

    @Column(length = 100)
    val contentType: String?,

    @Column(nullable = false, length = 500)
    val s3Key: String,

    @Column(nullable = false, length = 100)
    val s3Bucket: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var uploadStatus: UploadStatus = UploadStatus.UPLOADING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    val uploader: User,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

enum class UploadStatus {
    UPLOADING, COMPLETED, FAILED
}

@Entity
@Table(name = "post_files")
data class PostFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    val file: File,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)