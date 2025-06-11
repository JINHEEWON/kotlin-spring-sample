package com.example.kotlinsample.member.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "members",
    indexes = [
        Index(name = "idx_username", columnList = "username"),
        Index(name = "idx_email", columnList = "email"),
        Index(name = "idx_status", columnList = "status"),
        Index(name = "idx_created_at", columnList = "createdAt")
    ]
)
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 50)
    val username: String,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 20)
    var phone: String? = null,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

    @Enumerated(EnumType.STRING)
    var status: MemberStatus = MemberStatus.ACTIVE,

    @Enumerated(EnumType.STRING)
    var role: MemberRole = MemberRole.USER,

    @Column(columnDefinition = "TEXT")
    var address: String? = null,

    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // 비밀번호 변경 메서드
    fun changePassword(newPassword: String) {
        this.password = newPassword
    }

    // 프로필 업데이트 메서드
    fun updateProfile(
        name: String? = null,
        phone: String? = null,
        address: String? = null,
        profileImageUrl: String? = null
    ) {
        name?.let { this.name = it }
        phone?.let { this.phone = it }
        address?.let { this.address = it }
        profileImageUrl?.let { this.profileImageUrl = it }
    }

    // 마지막 로그인 시간 업데이트
    fun updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now()
    }

    // 계정 상태 변경
    fun changeStatus(newStatus: MemberStatus) {
        this.status = newStatus
    }

    // 역할 변경
    fun changeRole(newRole: MemberRole) {
        this.role = newRole
    }

    // 활성 사용자 여부 확인
    fun isActive(): Boolean = status == MemberStatus.ACTIVE

    // 관리자 여부 확인
    fun isAdmin(): Boolean = role == MemberRole.ADMIN

    // 매니저 이상 권한 확인
    fun hasManagerPermission(): Boolean = role in listOf(MemberRole.ADMIN, MemberRole.MANAGER)
}

// 성별 Enum
enum class Gender(val displayName: String) {
    M("남성"),
    F("여성"),
    OTHER("기타")
}

// 멤버 상태 Enum
enum class MemberStatus(val displayName: String) {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    SUSPENDED("정지")
}

// 멤버 역할 Enum
enum class MemberRole(val displayName: String, val level: Int) {
    USER("사용자", 1),
    MANAGER("매니저", 2),
    ADMIN("관리자", 3);

    // 권한 레벨 비교
    fun hasHigherOrEqualLevel(other: MemberRole): Boolean {
        return this.level >= other.level
    }
}