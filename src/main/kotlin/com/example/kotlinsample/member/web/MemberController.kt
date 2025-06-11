package com.example.kotlinsample.member.web

import com.example.kotlinsample.member.domain.dto.*
import com.example.kotlinsample.member.domain.model.MemberRole
import com.example.kotlinsample.member.domain.model.MemberStatus
import com.example.kotlinsample.member.domain.service.MemberService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = ["*"])
class MemberController(
    private val memberService: MemberService
) {

    // 회원 가입
    @PostMapping("/register")
    fun registerMember(@Valid @RequestBody request: MemberRegistrationRequest): ResponseEntity<ApiResponse<MemberResponse>> {
        return try {
            val member = memberService.registerMember(request)
            ResponseEntity.ok(ApiResponse.success(member, "회원가입이 완료되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "잘못된 요청입니다"))
        }
    }

    // 로그인
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        return try {
            val member = memberService.login(request)
            // 실제로는 JWT 토큰을 생성해야 함
            val token = "generated-jwt-token-here" // JWT 서비스에서 토큰 생성
            val loginResponse = LoginResponse(token, MemberResponse.from(member))
            ResponseEntity.ok(ApiResponse.success(loginResponse, "로그인 성공"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "로그인 실패"))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.message ?: "계정 상태 오류"))
        }
    }

    // 내 정보 조회
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getMyInfo(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<ApiResponse<MemberResponse>> {
        return try {
            val member = memberService.getMemberByUsername(userDetails.username)
            ResponseEntity.ok(ApiResponse.success(member))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // 특정 회원 조회
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or #id == authentication.principal.id")
    fun getMember(@PathVariable id: Long): ResponseEntity<ApiResponse<MemberResponse>> {
        return try {
            val member = memberService.getMemberById(id)
            ResponseEntity.ok(ApiResponse.success(member))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // 회원 목록 조회 (관리자/매니저만)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun getMembers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sort: String,
        @RequestParam(defaultValue = "DESC") direction: String,
        @RequestParam(required = false) status: MemberStatus?,
        @RequestParam(required = false) role: MemberRole?,
        @RequestParam(required = false) keyword: String?
    ): ResponseEntity<ApiResponse<PagedMemberResponse>> {
        val members = memberService.getMembers(page, size, sort, direction, status, role, keyword)
        return ResponseEntity.ok(ApiResponse.success(members))
    }

    // 내 정보 수정
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun updateMyInfo(
        @Valid @RequestBody request: MemberUpdateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<MemberResponse>> {
        return try {
            // 실제로는 UserDetails에서 사용자 ID를 가져와야 함
            val currentUser = memberService.getMemberByUsername(userDetails.username)
            val updatedMember = memberService.updateMember(currentUser.id, request)
            ResponseEntity.ok(ApiResponse.success(updatedMember, "정보가 업데이트되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "업데이트 실패"))
        }
    }

    // 특정 회원 정보 수정 (관리자/매니저만)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun updateMember(
        @PathVariable id: Long,
        @Valid @RequestBody request: MemberUpdateRequest
    ): ResponseEntity<ApiResponse<MemberResponse>> {
        return try {
            val updatedMember = memberService.updateMember(id, request)
            ResponseEntity.ok(ApiResponse.success(updatedMember, "회원 정보가 업데이트되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "업데이트 실패"))
        }
    }

    // 비밀번호 변경
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(
        @Valid @RequestBody request: PasswordChangeRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val currentUser = memberService.getMemberByUsername(userDetails.username)
            memberService.changePassword(currentUser.id, request)
            ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "비밀번호 변경 실패"))
        }
    }

    // 회원 상태/역할 변경 (관리자만)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateMemberStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: MemberStatusUpdateRequest
    ): ResponseEntity<ApiResponse<MemberResponse>> {
        return try {
            val updatedMember = memberService.updateMemberStatus(id, request)
            ResponseEntity.ok(ApiResponse.success(updatedMember, "회원 상태가 변경되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "상태 변경 실패"))
        }
    }

    // 회원 삭제 (관리자만)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteMember(@PathVariable id: Long): ResponseEntity<ApiResponse<String>> {
        return try {
            memberService.deleteMember(id)
            ResponseEntity.ok(ApiResponse.success("회원이 삭제되었습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "삭제 실패"))
        }
    }

    // 최근 로그인한 회원들 (관리자/매니저만)
    @GetMapping("/recent-logins")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun getRecentlyLoggedInMembers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<PagedMemberResponse>> {
        val members = memberService.getRecentlyLoggedInMembers(page, size)
        return ResponseEntity.ok(ApiResponse.success(members))
    }

    // 회원 통계 (관리자만)
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    fun getMemberStatistics(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val statistics = memberService.getMemberStatistics()
        return ResponseEntity.ok(ApiResponse.success(statistics))
    }

    // 사용자명 중복 체크
    @GetMapping("/check-username")
    fun checkUsernameAvailability(@RequestParam username: String): ResponseEntity<ApiResponse<Map<String, Boolean>>> {
        val isAvailable = memberService.checkUsernameAvailability(username)
        val result = mapOf("available" to isAvailable)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    // 이메일 중복 체크
    @GetMapping("/check-email")
    fun checkEmailAvailability(@RequestParam email: String): ResponseEntity<ApiResponse<Map<String, Boolean>>> {
        val isAvailable = memberService.checkEmailAvailability(email)
        val result = mapOf("available" to isAvailable)
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}

// API 응답 래퍼 클래스
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(true, message, data)
        }

        fun <T> error(message: String): ApiResponse<T> {
            return ApiResponse(false, message, null)
        }
    }
}