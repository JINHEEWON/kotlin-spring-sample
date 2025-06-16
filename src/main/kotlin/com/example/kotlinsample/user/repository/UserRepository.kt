package com.example.kotlinsample.user.repository

import com.example.kotlinsample.user.domain.model.User
import com.example.kotlinsample.user.domain.model.UserRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    fun findByEmail(email: String): User?

    // 이메일로 사용자 찾기 (삭제되지 않은 사용자만)
    fun findByEmailAndDeletedAtIsNull(email: String): User?

    // ID로 사용자 찾기 (삭제되지 않은 사용자만)
    fun findByIdAndDeletedAtIsNull(id: Long): User?

    // 이메일 중복 확인 (삭제되지 않은 사용자 기준)
    fun existsByEmailAndDeletedAtIsNull(email: String): Boolean

    // 전체 사용자 목록 조회 (삭제되지 않은 사용자만)
    fun findByDeletedAtIsNullOrderByCreatedAtDesc(pageable: Pageable): Page<User>

    // 역할별 사용자 조회
    fun findByRoleAndDeletedAtIsNullOrderByCreatedAtDesc(
        role: UserRole,
        pageable: Pageable
    ): Page<User>

    // 사용자 검색 (이름 또는 이메일)
    @Query("""
        SELECT u FROM User u 
        WHERE u.deletedAt IS NULL 
        AND (u.name LIKE %:keyword% OR u.email LIKE %:keyword%)
        ORDER BY u.createdAt DESC
    """)
    fun searchByKeyword(@Param("keyword") keyword: String, pageable: Pageable): Page<User>

    // 전체 사용자 수 (삭제되지 않은 사용자)
    fun countByDeletedAtIsNull(): Long

    // 역할별 사용자 수
    fun countByRoleAndDeletedAtIsNull(role: UserRole): Long

    // 특정 기간 내 가입한 사용자 수
    fun countByCreatedAtBetweenAndDeletedAtIsNull(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long

    // 오늘 가입한 사용자 수
    @Query("""
        SELECT COUNT(u) FROM User u 
        WHERE u.deletedAt IS NULL 
        AND DATE(u.createdAt) = CURRENT_DATE
    """)
    fun countTodayRegisteredUsers(): Long

    // 활성 사용자 수 (최근 30일 내 활동)
    @Query("""
        SELECT COUNT(DISTINCT u) FROM User u 
        WHERE u.deletedAt IS NULL 
        AND u.updatedAt >= :thirtyDaysAgo
    """)
    fun countActiveUsers(@Param("thirtyDaysAgo") thirtyDaysAgo: LocalDateTime): Long
}