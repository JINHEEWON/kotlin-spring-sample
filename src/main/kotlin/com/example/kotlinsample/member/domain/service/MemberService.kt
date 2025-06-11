package com.example.kotlinsample.member.domain.service

import com.example.kotlinsample.member.domain.Member
import com.example.kotlinsample.member.repository.MemberRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {

    // 회원 가입
    @Transactional
    fun registerMember(request: MemberRegistrationRequest): MemberResponse {
        // 중복 체크
        if (memberRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("이미 사용 중인 사용자명입니다: ${request.username}")
        }

        if (memberRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다: ${request.email}")
        }

        // 새 회원 생성
        val member = Member(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            phone = request.phone,
            birthDate = request.birthDate,
            gender = request.gender,
            address = request.address
        )

        val savedMember = memberRepository.save(member)
        return MemberResponse.from(savedMember)
    }

    // 로그인
    @Transactional
    fun login(request: LoginRequest): Member {
        val member = memberRepository.findByUsernameOrEmail(request.usernameOrEmail, request.usernameOrEmail)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다") }

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw IllegalArgumentException("비밀번호가 올바르지 않습니다")
        }

        if (member.status != MemberStatus.ACTIVE) {
            throw IllegalStateException("비활성화된 계정입니다. 상태: ${member.status}")
        }

        // 마지막 로그인 시간 업데이트
        member.updateLastLogin()
        memberRepository.save(member)

        return member
    }

    // ID로 회원 조회
    fun getMemberById(id: Long): MemberResponse {
        val member = memberRepository.findById(id)
            .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다: $id") }
        return MemberResponse.from(member)
    }

    // 사용자명으로 회원 조회
    fun getMemberByUsername(username: String): MemberResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다: $username") }
        return MemberResponse.from(member)
    }

    // 회원 목록 조회 (페이징)
    fun getMembers(
        page: Int = 0,
        size: Int = 20,
        sort: String = "createdAt",
        direction: String = "DESC",
        status: MemberStatus? = null,
        role: MemberRole? = null,
        keyword: String? = null
    ): PagedMemberResponse {
        val pageable: Pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.fromString(direction), sort)
        )

        val memberPage = when {
            !keyword.isNullOrBlank() -> {
                memberRepository.findActiveByKeyword(MemberStatus.ACTIVE, keyword, pageable)
            }
            status != null && role != null -> {
                memberRepository.findByStatusAndRole(status, role, pageable)
            }
            status != null -> {
                memberRepository.findByStatus(status, pageable)
            }
            role != null -> {
                memberRepository.findByRole(role, pageable)
            }
            else -> {
                memberRepository.findAll(pageable)
            }
        }

        val memberSummaries = memberPage.content.map { MemberSummary.from(it) }

        return PagedMemberResponse(
            content = memberSummaries,
            totalElements = memberPage.totalElements,
            totalPages = memberPage.totalPages,
            currentPage = page,
            size = size,
            hasNext = memberPage.hasNext(),
            hasPrevious = memberPage.hasPrevious()
        )
    }

    // 회원 정보 업데이트
    @Transactional
    fun updateMember(id: Long, request: MemberUpdateRequest): MemberResponse {
        val member = memberRepository.findById(id)
            .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다: $id") }

        member.updateProfile(
            name = request.name,
            phone = request.phone,
            address = request.address,
            profileImageUrl = request.profileImageUrl
        )

        request.birthDate?.let { member.birthDate = it }
        request.gender?.let { member.gender = it }

        val updatedMember = memberRepository.save(member)
        return MemberResponse.from(updatedMember)
    }

    // 비밀번호 변경
    @Transactional
    fun changePassword(id: Long, request: PasswordChangeRequest): Boolean {
        val member = memberRepository.findById(id)
            .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다: $id") }

        if (!passwordEncoder.matches(request.currentPassword, member.password)) {
            throw IllegalArgumentException("현재 비밀번호가 올바르지 않습니다")
        }

        member.changePassword(passwordEncoder.encode(request.newPassword))
        memberRepository.save(member)
        return true
    }

    // 회원 상태 변경 (관리자용)
    @Transactional
    fun updateMemberStatus(id: Long, request: MemberStatusUpdateRequest): MemberResponse {
        val member = memberRepository.findById(id)
            .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다: $id") }

        request.status?.let { member.changeStatus(it) }
        request.role?.let { member.changeRole(it) }

        val updatedMember = memberRepository.save(member)
        return MemberResponse.from(updatedMember)
    }

    // 회원 삭제 (soft delete - 상태를 INACTIVE로 변경)
    @Transactional
    fun deleteMember(id: Long): Boolean {
        val member = memberRepository.findById(id)
            .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다: $id") }

        member.changeStatus(MemberStatus.INACTIVE)
        memberRepository.save(member)
        return true
    }

    // 최근 로그인한 회원들 조회
    fun getRecentlyLoggedInMembers(page: Int = 0, size: Int = 10): PagedMemberResponse {
        val pageable = PageRequest.of(page, size)
        val memberPage = memberRepository.findRecentlyLoggedInMembers(MemberStatus.ACTIVE, pageable)

        val memberSummaries = memberPage.content.map { MemberSummary.from(it) }

        return PagedMemberResponse(
            content = memberSummaries,
            totalElements = memberPage.totalElements,
            totalPages = memberPage.totalPages,
            currentPage = page,
            size = size,
            hasNext = memberPage.hasNext(),
            hasPrevious = memberPage.hasPrevious()
        )
    }

    // 통계 정보 조회
    fun getMemberStatistics(): Map<String, Any> {
        return mapOf(
            "totalMembers" to memberRepository.count(),
            "activeMembers" to memberRepository.countByStatus(MemberStatus.ACTIVE),
            "inactiveMembers" to memberRepository.countByStatus(MemberStatus.INACTIVE),
            "suspendedMembers" to memberRepository.countByStatus(MemberStatus.SUSPENDED),
            "adminCount" to memberRepository.countByRole(MemberRole.ADMIN),
            "managerCount" to memberRepository.countByRole(MemberRole.MANAGER),
            "userCount" to memberRepository.countByRole(MemberRole.USER),
            "todayRegistrations" to memberRepository.countTodayRegistrations(),
            "thisMonthRegistrations" to memberRepository.countThisMonthRegistrations()
        )
    }

    // 사용자명/이메일 중복 체크
    fun checkUsernameAvailability(username: String): Boolean {
        return !memberRepository.existsByUsername(username)
    }

    fun checkEmailAvailability(email: String): Boolean {
        return !memberRepository.existsByEmail(email)
    }
}