package com.example.kotlinsample.security.jwt

import com.example.kotlinsample.common.exception.InvalidTokenException
import com.example.kotlinsample.common.exception.TokenExpiredException
import com.example.kotlinsample.security.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = getTokenFromRequest(request)

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val email = jwtTokenProvider.getEmailFromToken(token)

                // SecurityContext에 이미 인증 정보가 있는지 확인
                if (SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(email)

                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (ex: TokenExpiredException) {
            logger.warn("JWT token expired: ${ex.message}")
            handleTokenException(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다")
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다")
            return
        } catch (ex: InvalidTokenException) {
            logger.warn("Invalid JWT token: ${ex.message}")
            handleTokenException(response, "INVALID_TOKEN", "유효하지 않은 토큰입니다")
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다")
            return
        } catch (ex: Exception) {
            logger.error("JWT authentication error", ex)
            handleTokenException(response, "AUTHENTICATION_ERROR", "인증 처리 중 오류가 발생했습니다")
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 처리 중 오류가 발생했습니다")
            return
        }

        filterChain.doFilter(request, response)
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     */
    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    /**
     * 토큰 예외 처리
     */
    private fun handleTokenException(response: HttpServletResponse, code: String, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"

        val errorResponse = """
            {
                "error": {
                    "code": "$code",
                    "message": "$message"
                }
            }
        """.trimIndent()

        response.writer.write(errorResponse)
        response.writer.flush()
    }
}