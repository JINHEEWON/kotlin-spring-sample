package com.example.kotlinsample.security.jwt

import com.example.kotlinsample.common.exception.InvalidTokenException
import com.example.kotlinsample.common.exception.TokenExpiredException
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider {

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.expiration}")
    private var jwtExpiration: Long = 86400 // 24시간 (초)

    @Value("\${jwt.refresh-expiration}")
    private var refreshExpiration: Long = 604800 // 7일 (초)

    private lateinit var key: Key

    @PostConstruct
    fun init() {
        key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Access Token 생성
     */
    fun generateAccessToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal
        val email = when (userPrincipal) {
            is String -> userPrincipal
            else -> authentication.name
        }

        val authorities = authentication.authorities.joinToString(",") { it.authority }

        return Jwts.builder()
            .setSubject(email)
            .claim("authorities", authorities)
            .claim("type", "access")
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpiration * 1000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal
        val email = when (userPrincipal) {
            is String -> userPrincipal
            else -> authentication.name
        }

        return Jwts.builder()
            .setSubject(email)
            .claim("type", "refresh")
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + refreshExpiration * 1000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 토큰에서 이메일 추출
     */
    fun getEmailFromToken(token: String): String {
        return getClaims(token).subject
    }

    /**
     * 토큰에서 권한 추출
     */
    fun getAuthoritiesFromToken(token: String): List<String> {
        val authoritiesString = getClaims(token)["authorities"] as String?
        return authoritiesString?.split(",") ?: emptyList()
    }

    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (ex: JwtException) {
            when (ex) {
                is ExpiredJwtException -> throw TokenExpiredException("토큰이 만료되었습니다")
                is UnsupportedJwtException -> throw InvalidTokenException("지원되지 않는 토큰 형식입니다")
                is MalformedJwtException -> throw InvalidTokenException("잘못된 토큰 형식입니다")
                is SecurityException -> throw InvalidTokenException("토큰 서명이 유효하지 않습니다")
                is IllegalArgumentException -> throw InvalidTokenException("토큰이 비어있습니다")
                else -> throw InvalidTokenException("토큰 검증에 실패했습니다")
            }
        } catch (ex: Exception) {
            throw InvalidTokenException("토큰 처리 중 오류가 발생했습니다")
        }
    }

    /**
     * 토큰에서 Claims 추출
     */
    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }

    /**
     * Access Token 만료 시간 반환 (초 단위)
     */
    fun getAccessTokenExpiration(): Long {
        return jwtExpiration
    }

    /**
     * Refresh Token 만료 시간 반환 (초 단위)
     */
    fun getRefreshTokenExpiration(): Long {
        return refreshExpiration
    }

    /**
     * 토큰 타입 확인
     */
    fun getTokenType(token: String): String {
        return getClaims(token)["type"] as String? ?: "access"
    }

    /**
     * 토큰 만료 시간 확인
     */
    fun getExpirationFromToken(token: String): Date {
        return getClaims(token).expiration
    }

    /**
     * 토큰이 곧 만료되는지 확인 (갱신 필요 여부)
     */
    fun isTokenExpiringSoon(token: String, minutesBeforeExpiry: Long = 10): Boolean {
        val expiration = getExpirationFromToken(token)
        val now = Date()
        val threshold = Date(now.time + minutesBeforeExpiry * 60 * 1000)
        return expiration.before(threshold)
    }
}
