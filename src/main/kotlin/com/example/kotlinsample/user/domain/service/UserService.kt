package com.example.kotlinsample.user.domain.service

import com.example.kotlinsample.common.exception.BadRequestException
import com.example.kotlinsample.common.exception.ConflictException
import com.example.kotlinsample.common.exception.ResourceNotFoundException
import com.example.kotlinsample.common.exception.UnauthorizedException
import com.example.kotlinsample.file.repository.FileRepository
import com.example.kotlinsample.post.domain.model.PostStatus
import com.example.kotlinsample.post.repository.PostRepository
import com.example.kotlinsample.user.domain.dto.*
import com.example.kotlinsample.user.domain.model.User
import com.example.kotlinsample.user.domain.model.UserRole
import com.example.kotlinsample.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val fileRepository: FileRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional(readOnly = true)
    fun getAllUsers(
        page: Int = 0,
        size: Int = 10,
        sort: String = "createdAt",
        direction: String = "desc",
        search: String? = null,
        role: UserRole? = null
    ): UserListResponse {
        val pageable = createPageable(page, size, sort, direction)

        val users = when {
            search != null -> userRepository.searchByKeyword(search, pageable)
            role != null -> userRepository.findByRoleAndDeletedAtIsNullOrderByCreatedAtDesc(role, pageable)
            else -> userRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
        }

        val userSummaries = users.map { user ->
            UserSummaryResponse(
                id = user.id!!,
                email = user.email,
                name = user.name,
                role = user.role,
                createdAt = user.createdAt!!,
                isActive = user.deletedAt == null
            )
        }

        return UserListResponse(userSummaries)
    }

    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserDetailResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(id)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        val postCount = postRepository.countByAuthorAndDeletedAtIsNull(user)
        val fileCount = fileRepository.countByUploader(user)

        return UserDetailResponse(
            id = user.id!!,
            email = user.email,
            name = user.name,
            role = user.role,
            createdAt = user.createdAt!!,
            updatedAt = user.updatedAt!!,
            isActive = user.deletedAt == null,
            postCount = postCount,
            fileCount = fileCount
        )
    }

    @Transactional(readOnly = true)
    fun getMyProfile(userId: Long): ProfileResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        val statistics = getUserStatistics(user)

        return ProfileResponse(
            id = user.id!!,
            email = user.email,
            name = user.name,
            role = user.role,
            createdAt = user.createdAt!!,
            statistics = statistics
        )
    }

    fun updateProfile(userId: Long, request: UserUpdateRequest): UserDetailResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        // 이메일 변경 시 중복 확인
        request.email?.let { newEmail ->
            if (newEmail != user.email && userRepository.existsByEmailAndDeletedAtIsNull(newEmail)) {
                throw ConflictException("이미 사용 중인 이메일입니다")
            }
            // 실제로는 이메일 변경 시 인증 과정이 필요할 수 있음
        }

        // 필드 업데이트
        request.name?.let { user.name = it }
        request.email?.let { user.email = it }

        val updatedUser = userRepository.save(user)
        return getUserById(updatedUser.id!!)
    }

    fun changePassword(userId: Long, request: PasswordChangeRequest) {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw BadRequestException("현재 비밀번호가 올바르지 않습니다")
        }

        // 새 비밀번호와 현재 비밀번호가 같은지 확인
        if (passwordEncoder.matches(request.newPassword, user.password)) {
            throw BadRequestException("새 비밀번호는 현재 비밀번호와 달라야 합니다")
        }

        // 비밀번호 변경
        user.password = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)
    }

    fun deleteAccount(userId: Long) {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        // 소프트 삭제
        user.deletedAt = LocalDateTime.now()
        userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun searchUsers(
        keyword: String,
        page: Int = 0,
        size: Int = 10
    ): UserListResponse {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val users = userRepository.searchByKeyword(keyword, pageable)

        val userSummaries = users.map { user ->
            UserSummaryResponse(
                id = user.id!!,
                email = user.email,
                name = user.name,
                role = user.role,
                createdAt = user.createdAt!!,
                isActive = user.deletedAt == null
            )
        }

        return UserListResponse(userSummaries)
    }

    // 관리자 전용 메서드들
    fun updateUserRole(userId: Long, request: UserRoleUpdateRequest, adminId: Long): UserDetailResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        val admin = userRepository.findByIdAndDeletedAtIsNull(adminId)
            ?: throw ResourceNotFoundException("관리자를 찾을 수 없습니다")

        if (admin.role != UserRole.ADMIN) {
            throw UnauthorizedException("관리자 권한이 필요합니다")
        }

        // 자기 자신의 역할은 변경할 수 없음
        if (userId == adminId) {
            throw BadRequestException("자신의 역할은 변경할 수 없습니다")
        }

        user.role = request.role
        val updatedUser = userRepository.save(user)

        return getUserById(updatedUser.id!!)
    }

    fun deleteUser(userId: Long, adminId: Long) {
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")

        val admin = userRepository.findByIdAndDeletedAtIsNull(adminId)
            ?: throw ResourceNotFoundException("관리자를 찾을 수 없습니다")

        if (admin.role != UserRole.ADMIN) {
            throw UnauthorizedException("관리자 권한이 필요합니다")
        }

        // 자기 자신은 삭제할 수 없음
        if (userId == adminId) {
            throw BadRequestException("자신의 계정은 삭제할 수 없습니다")
        }

        // 소프트 삭제
        user.deletedAt = LocalDateTime.now()
        userRepository.save(user)
    }

    private fun createPageable(page: Int, size: Int, sort: String, direction: String): Pageable {
        val sortDirection = if (direction.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val sortBy = when (sort) {
            "name" -> "name"
            "email" -> "email"
            "role" -> "role"
            else -> "createdAt"
        }
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
    }

    private fun getUserStatistics(user: User): UserStatistics {
        val totalPostCount = postRepository.countByAuthorAndDeletedAtIsNull(user)
        val publishedPostCount = postRepository.countByAuthorAndStatusAndDeletedAtIsNull(user, PostStatus.PUBLISHED)
        val draftPostCount = postRepository.countByAuthorAndStatusAndDeletedAtIsNull(user, PostStatus.DRAFT)
        val fileCount = fileRepository.countByUploader(user)
        val totalFileSize = fileRepository.sumFileSizeByUploaderAndDeletedAtIsNull(user) ?: 0L

        return UserStatistics(
            postCount = totalPostCount,
            publishedPostCount = publishedPostCount,
            draftPostCount = draftPostCount,
            fileCount = fileCount,
            totalFileSize = totalFileSize
        )
    }
}