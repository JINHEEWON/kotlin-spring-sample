package com.example.kotlinsample.post.repository

import com.example.kotlinsample.post.domain.model.Post
import com.example.kotlinsample.post.domain.model.PostStatus
import com.example.kotlinsample.user.domain.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long> {

    // 삭제되지 않은 게시글 조회
    fun findByIdAndDeletedAtIsNull(id: Long): Post?

    // 전체 게시글 목록 (삭제되지 않은 것만)
    fun findByDeletedAtIsNullOrderByCreatedAtDesc(pageable: Pageable): Page<Post>

    // 상태별 게시글 조회
    fun findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
        status: PostStatus,
        pageable: Pageable
    ): Page<Post>

    // 특정 사용자의 게시글 조회
    fun findByAuthorAndDeletedAtIsNullOrderByCreatedAtDesc(
        author: User,
        pageable: Pageable
    ): Page<Post>

    // 특정 사용자의 상태별 게시글 조회
    fun findByAuthorAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
        author: User,
        status: PostStatus,
        pageable: Pageable
    ): Page<Post>

    // 제목/내용으로 검색
    @Query("""
        SELECT p FROM Post p 
        WHERE p.deletedAt IS NULL 
        AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)
        ORDER BY p.createdAt DESC
    """)
    fun searchByKeyword(@Param("keyword") keyword: String, pageable: Pageable): Page<Post>

    // 게시 상태인 게시글만 검색 (비회원도 볼 수 있는)
    @Query("""
        SELECT p FROM Post p 
        WHERE p.deletedAt IS NULL 
        AND p.status = 'PUBLISHED'
        AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)
        ORDER BY p.createdAt DESC
    """)
    fun searchPublishedByKeyword(@Param("keyword") keyword: String, pageable: Pageable): Page<Post>

    // 작성자별 게시글 수 조회
    fun countByAuthorAndDeletedAtIsNull(author: User): Long

    // 상태별 게시글 수 조회
    fun countByStatusAndDeletedAtIsNull(status: PostStatus): Long

    // 최근 게시글 조회 (메인 페이지용)
    @Query("""
        SELECT p FROM Post p 
        WHERE p.deletedAt IS NULL 
        AND p.status = 'PUBLISHED'
        ORDER BY p.createdAt DESC
    """)
    fun findRecentPublishedPosts(pageable: Pageable): Page<Post>
    fun countByAuthorAndStatusAndDeletedAtIsNull(user: User, published: PostStatus): Long
}