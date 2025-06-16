package com.example.kotlinsample.auth.service

import com.example.kotlinsample.auth.dto.*
import com.example.kotlinsample.common.exception.*
import com.example.kotlinsample.security.jwt.JwtTokenProvider
import com.example.kotlinsample.user.domain.model.User
import com.example.kotlinsample.user.domain.model.UserRole
import com.example.kotlinsample.user.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * 사용자 로그인
     */
    fun login(loginRequest: LoginRequest): LoginResponse {
        try {

            // 1. 사용자 존재 여부 확인
            val user = userRepository.findByEmailAndDeletedAtIsNull(loginRequest.email)
                ?: throw InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다")


            // DB에 저장된 실제 비밀번호 확인
            println("DB password: ${user.password}")

            // 입력받은 평문 비밀번호 확인
            println("Input password: ${loginRequest.password}")

            // 인코더로 매칭 테스트
            val matches = passwordEncoder.matches(loginRequest.password, user.password)
            println("Password matches: $matches")


            // 2. 인증 처리
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.password
                )
            )

//            // 1. AuthenticationManager에게 모든 인증 위임
//            val authentication = authenticationManager.authenticate(
//                UsernamePasswordAuthenticationToken(
//                    loginRequest.email,
//                    loginRequest.password
//                )
//            )
//
//            // 2. 인증 성공 후 사용자 정보 조회
//            val user = userRepository.findByEmailAndDeletedAtIsNull(loginRequest.email)
//                ?: throw InvalidCredentialsException("인증에 실패했습니다")

            // 3. SecurityContext에 Authentication 설정
            SecurityContextHolder.getContext().authentication = authentication

            // 4. JWT 토큰 생성
            val accessToken = jwtTokenProvider.generateAccessToken(authentication)
            val refreshToken = jwtTokenProvider.generateRefreshToken(authentication)

            return LoginResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = jwtTokenProvider.getAccessTokenExpiration(),
                user = LoginResponse.UserInfo(
                    id = user.id!!,
                    email = user.email,
                    name = user.name,
                    role = user.role,
                    createdAt = user.createdAt!!
                )
            )

        } catch (ex: BadCredentialsException) {
            throw InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다")
        }
    }

    /**
     * 사용자 회원가입
     */
    fun register(registerRequest: RegisterRequest): RegisterResponse {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmailAndDeletedAtIsNull(registerRequest.email)) {
            throw ConflictException("이미 사용 중인 이메일입니다")
        }

        // 2. 새 사용자 생성
        val user = User(
            name = registerRequest.name,
            email = registerRequest.email,
            password = passwordEncoder.encode(registerRequest.password),
            role = UserRole.USER
        )

        val savedUser = userRepository.save(user)

        return RegisterResponse(
            user = RegisterResponse.UserInfo(
                id = savedUser.id!!,
                email = savedUser.email,
                name = savedUser.name,
                role = savedUser.role,
                createdAt = savedUser.createdAt!!
            )
        )
    }

    /**
     * 액세스 토큰 갱신
     */
    fun refreshToken(tokenRefreshRequest: TokenRefreshRequest): LoginResponse {
        val refreshToken = tokenRefreshRequest.refreshToken

        try {
            // 1. Refresh Token 유효성 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw InvalidTokenException("유효하지 않은 refresh token입니다")
            }

            // 2. Refresh Token에서 사용자 정보 추출
            val email = jwtTokenProvider.getEmailFromToken(refreshToken)
            val user = userRepository.findByEmailAndDeletedAtIsNull(email)
                ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

            // 3. 새로운 Authentication 객체 생성
            val authorities = listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_${user.role.name}"))
            val authentication = UsernamePasswordAuthenticationToken(user.email, null, authorities)

            // 4. 새로운 토큰 생성
            val newAccessToken = jwtTokenProvider.generateAccessToken(authentication)
            val newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication)

            return LoginResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                expiresIn = jwtTokenProvider.getAccessTokenExpiration(),
                user = LoginResponse.UserInfo(
                    id = user.id!!,
                    email = user.email,
                    name = user.name,
                    role = user.role,
                    createdAt = user.createdAt!!
                )
            )

        } catch (ex: Exception) {
            when (ex) {
                is InvalidTokenException, is ResourceNotFoundException -> throw ex
                else -> throw InvalidTokenException("토큰 갱신에 실패했습니다")
            }
        }
    }

    /**
     * 로그아웃
     */
    fun logout(): String {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication != null && authentication.isAuthenticated) {
            val email = authentication.name

            // 로그아웃 로그 기록
            println("사용자 로그아웃: $email")

            // SecurityContext 클리어
            SecurityContextHolder.clearContext()

            // TODO: 토큰 블랙리스트 추가 (필요한 경우)
            // blacklistService.addToBlacklist(token)
        }

        return "로그아웃이 완료되었습니다"
    }

    /**
     * 현재 인증된 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    fun getCurrentUser(email: String): LoginResponse.UserInfo {
        val user = userRepository.findByEmailAndDeletedAtIsNull(email)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        return LoginResponse.UserInfo(
            id = user.id!!,
            email = user.email,
            name = user.name,
            role = user.role,
            createdAt = user.createdAt!!
        )
    }
}