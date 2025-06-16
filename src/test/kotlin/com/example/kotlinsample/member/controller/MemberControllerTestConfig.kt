package com.example.kotlinsample.member.controller

import com.example.kotlinsample.member.domain.service.MemberService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class MemberControllerTestConfig {

    @Bean
    @Primary
    fun memberService(): MemberService = mockk(relaxed = true)
}