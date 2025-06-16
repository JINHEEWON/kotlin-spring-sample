package com.example.kotlinsample.user.web

import com.example.kotlinsample.member.web.ApiResponse
import com.example.kotlinsample.security.UserPrincipal
import com.example.kotlinsample.user.domain.dto.*
import com.example.kotlinsample.user.domain.model.UserRole
import com.example.kotlinsample.user.domain.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    /**
     * 사용자 목록 조회 (관리자 전용)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sort: String,
        @RequestParam(defaultValue = "desc") direction: String,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) role: UserRole?
    ): ResponseEntity<ApiResponse<UserListResponse>> {
        val users = userService.getAllUsers(page, size, sort, direction, search, role)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = users,
                message = "사용자 목록을 조회했습니다"
            )
        )
    }

    /**
     * 특정 사용자 상세 조회 (관리자 전용)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<UserDetailResponse>> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = user,
                message = "사용자 정보를 조회했습니다"
            )
        )
    }

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun getMyProfile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProfileResponse>> {
        val profile = userService.getMyProfile(userPrincipal.id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = profile,
                message = "프로필을 조회했습니다"
            )
        )
    }

    /**
     * 내 프로필 수정
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun updateMyProfile(
        @Valid @RequestBody request: UserUpdateRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserDetailResponse>> {
        val updatedUser = userService.updateProfile(userPrincipal.id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = updatedUser,
                message = "프로필이 수정되었습니다"
            )
        )
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/me/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun changePassword(
        @Valid @RequestBody request: PasswordChangeRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Nothing>> {
        userService.changePassword(userPrincipal.id, request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = null,
                message = "비밀번호가 변경되었습니다"
            )
        )
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun deleteMyAccount(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Nothing>> {
        userService.deleteAccount(userPrincipal.id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = null,
                message = "계정이 삭제되었습니다"
            )
        )
    }

    /**
     * 사용자 검색 (관리자 전용)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    fun searchUsers(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<UserListResponse>> {
        val users = userService.searchUsers(keyword, page, size)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = users,
                message = "사용자 검색 결과입니다"
            )
        )
    }

    /**
     * 사용자 역할 변경 (관리자 전용)
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUserRole(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserRoleUpdateRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserDetailResponse>> {
        val updatedUser = userService.updateUserRole(id, request, userPrincipal.id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = updatedUser,
                message = "사용자 역할이 변경되었습니다"
            )
        )
    }

    /**
     * 사용자 삭제 (관리자 전용)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Nothing>> {
        userService.deleteUser(id, userPrincipal.id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = null,
                message = "사용자가 삭제되었습니다"
            )
        )
    }
}