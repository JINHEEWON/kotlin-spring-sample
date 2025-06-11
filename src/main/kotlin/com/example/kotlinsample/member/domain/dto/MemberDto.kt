package com.example.kotlinsample.member.domain.dto

import com.example.kotlinsample.member.domain.model.Gender
import com.example.kotlinsample.member.domain.model.Member
import com.example.kotlinsample.member.domain.model.MemberRole
import com.example.kotlinsample.member.domain.model.MemberStatus
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime

// 회원 가입 요청 DTO
data class MemberRegistrationRequest(
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자명은 영문, 숫자, 언더스코어만 사용 가능합니다")
    val username: String,

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    val email: String,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    val password: String,

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
    val name: String,

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (010-1234-5678)")
    val phone: String? = null,

    @Past(message = "생년월일은 과거 날짜여야 합니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate? = null,

    val gender: Gender? = null,
    val address: String? = null
)

// 회원 정보 업데이트 요청 DTO
data class MemberUpdateRequest(
    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
    val name: String? = null,

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다")
    val phone: String? = null,

    @Past(message = "생년월일은 과거 날짜여야 합니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate? = null,

    val gender: Gender? = null,
    val address: String? = null,
    val profileImageUrl: String? = null
)

// 비밀번호 변경 요청 DTO
data class PasswordChangeRequest(
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,

    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    val newPassword: String
)

// 회원 응답 DTO (비밀번호 제외)
data class MemberResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val phone: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate?,
    val gender: Gender?,
    val status: MemberStatus,
    val role: MemberRole,
    val address: String?,
    val profileImageUrl: String?,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastLoginAt: LocalDateTime?,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(member: Member): MemberResponse {
            return MemberResponse(
                id = member.id,
                username = member.username,
                email = member.email,
                name = member.name,
                phone = member.phone,
                birthDate = member.birthDate,
                gender = member.gender,
                status = member.status,
                role = member.role,
                address = member.address,
                profileImageUrl = member.profileImageUrl,
                lastLoginAt = member.lastLoginAt,
                createdAt = member.createdAt,
                updatedAt = member.updatedAt
            )
        }
    }
}

// 간단한 회원 정보 DTO (목록 조회용)
data class MemberSummary(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val status: MemberStatus,
    val role: MemberRole,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(member: Member): MemberSummary {
            return MemberSummary(
                id = member.id,
                username = member.username,
                email = member.email,
                name = member.name,
                status = member.status,
                role = member.role,
                createdAt = member.createdAt
            )
        }
    }
}

// 로그인 요청 DTO
data class LoginRequest(
    @NotBlank(message = "사용자명 또는 이메일은 필수입니다")
    val usernameOrEmail: String,

    @NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)

// 로그인 응답 DTO
data class LoginResponse(
    val token: String,
    val member: MemberResponse
)

// 관리자용 회원 상태/역할 변경 DTO
data class MemberStatusUpdateRequest(
    val status: MemberStatus? = null,
    val role: MemberRole? = null
)

// 페이징된 회원 목록 응답 DTO
data class PagedMemberResponse(
    val content: List<MemberSummary>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)