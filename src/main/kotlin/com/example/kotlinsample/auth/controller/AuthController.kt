package com.example.kotlinsample.auth.controller

import com.example.kotlinsample.auth.dto.*
import com.example.kotlinsample.auth.service.AuthService
import com.example.kotlinsample.common.response.ApiResponse
import com.example.kotlinsample.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * 로그인
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.login(loginRequest)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = loginResponse,
                message = "로그인이 완료되었습니다"
            )
        )
    }

    /**
     * 회원가입
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<ApiResponse<RegisterResponse>> {
        val registerResponse = authService.register(registerRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse(
                    success = true,
                    data = registerResponse,
                    message = "회원가입이 완료되었습니다"
                )
            )
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody tokenRefreshRequest: TokenRefreshRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.refreshToken(tokenRefreshRequest)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = loginResponse,
                message = "토큰이 갱신되었습니다"
            )
        )
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<String>> {
        val message = authService.logout()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = message,
                message = "로그아웃이 완료되었습니다"
            )
        )
    }

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<LoginResponse.UserInfo>> {
        val userInfo = authService.getCurrentUser(userPrincipal.email)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = userInfo,
                message = "사용자 정보 조회 성공"
            )
        )
    }

    /**
     * 토큰 유효성 검증 (선택적 API)
     */
    @GetMapping("/validate")
    fun validateToken(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val response = mapOf(
            "valid" to true,
            "email" to userPrincipal.email,
            "role" to userPrincipal.authorities.first().authority
        )

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = response,
                message = "유효한 토큰입니다"
            )
        )
    }
}