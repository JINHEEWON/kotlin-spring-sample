package com.example.kotlinsample.file.repository

import com.example.kotlinsample.file.domain.model.File
import com.example.kotlinsample.file.domain.model.UploadStatus
import com.example.kotlinsample.user.domain.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<File, Long> {

    // 업로더와 상태로 파일 찾기
    fun findByIdAndUploaderIdAndUploadStatus(
        id: Long,
        uploaderId: Long,
        status: UploadStatus
    ): File?

    // 특정 사용자의 파일 개수
    fun countByUploader(uploader: User): Long

    // 특정 사용자의 총 파일 크기
    @Query("""
        SELECT COALESCE(SUM(f.fileSize), 0) FROM File f 
        WHERE f.uploader = :uploader 
        AND f.uploadStatus = 'COMPLETED'
    """)
    fun sumFileSizeByUploaderAndDeletedAtIsNull(@Param("uploader") uploader: User): Long?

    // 사용자별 파일 목록
    fun findByUploaderOrderByCreatedAtDesc(uploader: User, pageable: Pageable): Page<File>

    // 업로드 상태별 파일 조회
    fun findByUploadStatusOrderByCreatedAtDesc(status: UploadStatus, pageable: Pageable): Page<File>
}