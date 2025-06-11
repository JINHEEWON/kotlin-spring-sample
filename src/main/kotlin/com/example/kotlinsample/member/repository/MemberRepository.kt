package com.example.kotlinsample.member.repository

import com.example.kotlinsample.member.domain.model.Member
import com.example.kotlinsample.member.domain.model.MemberRole
import com.example.kotlinsample.member.domain.model.MemberStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface MemberRepository : JpaRepository<Member, Long> {

    // 기본 검색 메서드
    fun findByUsername(username: String): Optional<Member>
    fun findByEmail(email: String): Optional<Member>
    fun findByUsernameOrEmail(username: String, email: String): Optional<Member>

    // 존재 여부 확인
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean

    // 상태별 조회
    fun findByStatus(status: MemberStatus, pageable: Pageable): Page<Member>

    // 역할별 조회
    fun findByRole(role: MemberRole, pageable: Pageable): Page<Member>

    // 상태와 역할로 조회
    fun findByStatusAndRole(status: MemberStatus, role: MemberRole, pageable: Pageable): Page<Member>

    // 활성 사용자만 조회
    fun findByStatusAndUsernameContainingIgnoreCaseOrStatusAndNameContainingIgnoreCase(
        status1: MemberStatus,
        username: String,
        status2: MemberStatus,
        name: String,
        pageable: Pageable
    ): Page<Member>

    // 이름 또는 사용자명으로 검색 (활성 사용자만)
    @Query("""
        SELECT m FROM Member m 
        WHERE m.status = :status 
        AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR LOWER(m.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY m.createdAt DESC
    """)
    fun findActiveByKeyword(@Param("status") status: MemberStatus, @Param("keyword") keyword: String, pageable: Pageable): Page<Member>

    // 최근 로그인한 사용자들
    @Query("""
        SELECT m FROM Member m 
        WHERE m.lastLoginAt IS NOT NULL 
        AND m.status = :status
        ORDER BY m.lastLoginAt DESC
    """)
    fun findRecentlyLoggedInMembers(@Param("status") status: MemberStatus, pageable: Pageable): Page<Member>

    // 특정 기간 내 가입한 사용자
    @Query("""
        SELECT m FROM Member m 
        WHERE m.createdAt BETWEEN :startDate AND :endDate
        ORDER BY m.createdAt DESC
    """)
    fun findMembersByRegistrationPeriod(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Member>

    // 마지막 로그인이 특정 기간 이전인 사용자 (비활성 사용자 감지)
    @Query("""
        SELECT m FROM Member m 
        WHERE m.lastLoginAt < :cutoffDate 
        AND m.status = :status
        ORDER BY m.lastLoginAt ASC
    """)
    fun findInactiveMembersSince(
        @Param("cutoffDate") cutoffDate: LocalDateTime,
        @Param("status") status: MemberStatus,
        pageable: Pageable
    ): Page<Member>

    // 통계용 쿼리들
    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status")
    fun countByStatus(@Param("status") status: MemberStatus): Long

    @Query("SELECT COUNT(m) FROM Member m WHERE m.role = :role")
    fun countByRole(@Param("role") role: MemberRole): Long

    @Query("""
        SELECT COUNT(m) FROM Member m 
        WHERE m.createdAt >= :startDate 
        AND m.createdAt < :endDate
    """)
    fun countNewMembersInPeriod(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    // 오늘 가입한 사용자 수
    @Query("""
        SELECT COUNT(m) FROM Member m 
        WHERE DATE(m.createdAt) = CURRENT_DATE
    """)
    fun countTodayRegistrations(): Long

    // 이번 달 가입한 사용자 수
    @Query("""
        SELECT COUNT(m) FROM Member m 
        WHERE YEAR(m.createdAt) = YEAR(CURRENT_DATE) 
        AND MONTH(m.createdAt) = MONTH(CURRENT_DATE)
    """)
    fun countThisMonthRegistrations(): Long
}