package com.example.kotlinsample.member.controller

import com.example.kotlinsample.common.exception.GlobalExceptionHandler
import com.example.kotlinsample.member.domain.dto.*
import com.example.kotlinsample.member.domain.model.Gender
import com.example.kotlinsample.member.domain.model.Member
import com.example.kotlinsample.member.domain.model.MemberRole
import com.example.kotlinsample.member.domain.model.MemberStatus
import com.example.kotlinsample.member.domain.service.MemberService
import com.example.kotlinsample.member.web.MemberController
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [MemberController::class],
    excludeAutoConfiguration = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class]
)
@Import(GlobalExceptionHandler::class)
@ContextConfiguration(classes = [MemberController::class, MemberControllerTestConfig::class])
class MemberControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var sampleMemberResponse: MemberResponse
    private lateinit var sampleMemberDomain: Member

    @BeforeEach
    fun setUp() {
        sampleMemberResponse = MemberResponse(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            name = "Test User",
            phone = "010-1234-5678",
            birthDate = LocalDate.of(1990, 1, 1),
            gender = Gender.M,
            status = MemberStatus.ACTIVE,
            role = MemberRole.USER,
            address = "Test Address",
            profileImageUrl = null,
            lastLoginAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        sampleMemberDomain = mockk<Member>(relaxed = true) {
            every { id } returns 1L
            every { username } returns "testuser"
            every { email } returns "test@example.com"
            every { name } returns "Test User"
            every { status } returns MemberStatus.ACTIVE
            every { role } returns MemberRole.USER
            every { phone } returns "010-1234-5678"
            every { birthDate } returns LocalDate.of(1990, 1, 1)
            every { gender } returns Gender.M
            every { address } returns "Test Address"
            every { profileImageUrl } returns null
            every { lastLoginAt } returns null
            every { createdAt } returns LocalDateTime.now()
            every { updatedAt } returns LocalDateTime.now()
        }
    }

    @Test
    fun `회원가입 성공 테스트`() {
        // Given
        val request = MemberRegistrationRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            name = "Test User",
            phone = "010-1234-5678",
            birthDate = LocalDate.of(1990, 1, 1),
            gender = Gender.M,
            address = "Test Address"
        )

        every { memberService.registerMember(any()) } returns sampleMemberResponse

        // When & Then
        mockMvc.perform(
            post("/api/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
    }

    @Test
    fun `회원가입 실패 - Validation 오류`() {
        // Given
        val invalidRequest = mapOf(
            "username" to "",
            "email" to "invalid-email",
            "password" to "123",
            "name" to ""
        )

        // When & Then
        mockMvc.perform(
            post("/api/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `로그인 성공 테스트`() {
        // Given
        val request = LoginRequest(
            usernameOrEmail = "testuser",
            password = "password123"
        )

        every { memberService.login(any()) } returns sampleMemberDomain

        // When & Then
        mockMvc.perform(
            post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.member.username").value("testuser"))
    }

    @Test
    @WithMockUser(username = "testuser", authorities = ["ROLE_USER"])
    fun `내 정보 조회 테스트`() {
        // Given
        every { memberService.getMemberByUsername("testuser") } returns sampleMemberResponse

        // When & Then
        mockMvc.perform(get("/api/members/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"))
    }

    @Test
    fun `회원 목록 조회 테스트`() {
        // Given
        val memberSummary = MemberSummary(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            name = "Test User",
            status = MemberStatus.ACTIVE,
            role = MemberRole.USER,
            createdAt = LocalDateTime.now()
        )

        val pagedResponse = PagedMemberResponse(
            content = listOf(memberSummary),
            totalElements = 1L,
            totalPages = 1,
            currentPage = 0,
            size = 20,
            hasNext = false,
            hasPrevious = false
        )

        every { memberService.getMembers(0, 20, "createdAt", "DESC", null, null, null) } returns pagedResponse

        // When & Then
        mockMvc.perform(get("/api/members"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }

    @Test
    fun `사용자명 중복 체크 테스트`() {
        // Given
        every { memberService.checkUsernameAvailability("available") } returns true
        every { memberService.checkUsernameAvailability("taken") } returns false

        // When & Then
        mockMvc.perform(get("/api/members/check-username?username=available"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.available").value(true))
    }

    @Test
    fun `이메일 중복 체크 테스트`() {
        // Given
        every { memberService.checkEmailAvailability("available@example.com") } returns true

        // When & Then
        mockMvc.perform(get("/api/members/check-email?email=available@example.com"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.available").value(true))
    }
}