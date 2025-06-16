package com.example.kotlinsample.post.domain.service

import com.example.kotlinsample.common.exception.ResourceNotFoundException
import com.example.kotlinsample.common.exception.UnauthorizedException
import com.example.kotlinsample.file.domain.model.UploadStatus
import com.example.kotlinsample.file.repository.FileRepository
import com.example.kotlinsample.post.domain.dto.*
import com.example.kotlinsample.post.domain.model.Post
import com.example.kotlinsample.post.domain.model.PostStatus
import com.example.kotlinsample.post.repository.PostRepository
import com.example.kotlinsample.user.domain.model.User
import com.example.kotlinsample.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val fileRepository: FileRepository
) {

    @Transactional(readOnly = true)
    fun getAllPosts(
        page: Int = 0,
        size: Int = 10,
        sort: String = "createdAt",
        direction: String = "desc",
        search: String? = null,
        authorId: Long? = null,
        status: PostStatus? = null
    ): PostListResponse {
        val pageable = createPageable(page, size, sort, direction)

        val posts = when {
            search != null -> postRepository.searchByKeyword(search, pageable)
            authorId != null && status != null -> {
                val author = getUserById(authorId)
                postRepository.findByAuthorAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(author, status, pageable)
            }
            authorId != null -> {
                val author = getUserById(authorId)
                postRepository.findByAuthorAndDeletedAtIsNullOrderByCreatedAtDesc(author, pageable)
            }
            status != null -> postRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status, pageable)
            else -> postRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable)
        }

        val postSummaries = posts.map { post ->
            PostSummaryResponse(
                id = post.id!!,
                title = post.title,
                content = post.content?.take(100), // 미리보기용 100자만
                status = post.status,
                author = AuthorResponse(
                    id = post.author.id!!,
                    name = post.author.name
                ),
                createdAt = post.createdAt!!,
                fileCount = post.files.size
            )
        }

        return PostListResponse(postSummaries)
    }

    @Transactional(readOnly = true)
    fun getPublishedPosts(
        page: Int = 0,
        size: Int = 10,
        search: String? = null
    ): PostListResponse {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())

        val posts = if (search != null) {
            postRepository.searchPublishedByKeyword(search, pageable)
        } else {
            postRepository.findRecentPublishedPosts(pageable)
        }

        val postSummaries = posts.map { post ->
            PostSummaryResponse(
                id = post.id!!,
                title = post.title,
                content = post.content?.take(100),
                status = post.status,
                author = AuthorResponse(
                    id = post.author.id!!,
                    name = post.author.name
                ),
                createdAt = post.createdAt!!,
                fileCount = post.files.size
            )
        }

        return PostListResponse(postSummaries)
    }

    @Transactional(readOnly = true)
    fun getPostById(id: Long): PostResponse {
        val post = postRepository.findByIdAndDeletedAtIsNull(id)
            ?: throw ResourceNotFoundException("게시글을 찾을 수 없습니다")

        return convertToPostResponse(post)
    }

    @Transactional(readOnly = true)
    fun getMyPosts(
        userId: Long,
        page: Int = 0,
        size: Int = 10,
        status: PostStatus? = null
    ): PostListResponse {
        val user = getUserById(userId)
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())

        val posts = if (status != null) {
            postRepository.findByAuthorAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(user, status, pageable)
        } else {
            postRepository.findByAuthorAndDeletedAtIsNullOrderByCreatedAtDesc(user, pageable)
        }

        val postSummaries = posts.map { post ->
            PostSummaryResponse(
                id = post.id!!,
                title = post.title,
                content = post.content?.take(100),
                status = post.status,
                author = AuthorResponse(
                    id = post.author.id!!,
                    name = post.author.name
                ),
                createdAt = post.createdAt!!,
                fileCount = post.files.size
            )
        }

        return PostListResponse(postSummaries)
    }

    fun createPost(request: PostRequest, userId: Long): PostResponse {
        val author = getUserById(userId)

        val post = Post(
            title = request.title,
            content = request.content,
            author = author,
            status = request.status
        )

        val savedPost = postRepository.save(post)

        // 파일 연결 처리
        request.fileIds?.let { fileIds ->
            attachFilesToPost(savedPost, fileIds, userId)
        }

        return convertToPostResponse(savedPost)
    }

    fun updatePost(id: Long, request: PostUpdateRequest, userId: Long): PostResponse {
        val post = postRepository.findByIdAndDeletedAtIsNull(id)
            ?: throw ResourceNotFoundException("게시글을 찾을 수 없습니다")

        // 작성자 확인
        if (post.author.id != userId) {
            throw UnauthorizedException("게시글을 수정할 권한이 없습니다")
        }

        // 필드 업데이트
        request.title?.let { post.title = it }
        request.content?.let { post.content = it }
        request.status?.let { post.status = it }

        // 파일 연결 업데이트
        request.fileIds?.let { fileIds ->
            // 기존 파일 연결 제거
            post.files.clear()
            // 새 파일 연결
            attachFilesToPost(post, fileIds, userId)
        }

        val updatedPost = postRepository.save(post)
        return convertToPostResponse(updatedPost)
    }

    fun deletePost(id: Long, userId: Long) {
        val post = postRepository.findByIdAndDeletedAtIsNull(id)
            ?: throw ResourceNotFoundException("게시글을 찾을 수 없습니다")

        // 작성자 확인
        if (post.author.id != userId) {
            throw UnauthorizedException("게시글을 삭제할 권한이 없습니다")
        }

        // 소프트 삭제
        post.deletedAt = LocalDateTime.now()
        postRepository.save(post)
    }

    @Transactional(readOnly = true)
    fun searchPosts(
        keyword: String,
        page: Int = 0,
        size: Int = 10,
        publishedOnly: Boolean = false
    ): PostListResponse {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())

        val posts = if (publishedOnly) {
            postRepository.searchPublishedByKeyword(keyword, pageable)
        } else {
            postRepository.searchByKeyword(keyword, pageable)
        }

        val postSummaries = posts.map { post ->
            PostSummaryResponse(
                id = post.id!!,
                title = post.title,
                content = post.content?.take(100),
                status = post.status,
                author = AuthorResponse(
                    id = post.author.id!!,
                    name = post.author.name
                ),
                createdAt = post.createdAt!!,
                fileCount = post.files.size
            )
        }

        return PostListResponse(postSummaries)
    }

    private fun createPageable(page: Int, size: Int, sort: String, direction: String): Pageable {
        val sortDirection = if (direction.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val sortBy = when (sort) {
            "title" -> "title"
            "author" -> "author.name"
            else -> "createdAt"
        }
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
    }

    private fun getUserById(userId: Long): User {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw ResourceNotFoundException("사용자를 찾을 수 없습니다")
    }

    private fun attachFilesToPost(post: Post, fileIds: List<Long>, userId: Long) {
        // 파일 소유권 확인 및 연결
        fileIds.forEach { fileId ->
            val file = fileRepository.findByIdAndUploaderIdAndUploadStatus(
                fileId, userId, UploadStatus.COMPLETED
            ) ?: throw ResourceNotFoundException("파일을 찾을 수 없거나 권한이 없습니다: $fileId")

            // PostFile 엔티티 생성하여 연결
            // 실제 구현에서는 PostFile 엔티티의 save를 통해 처리
        }
    }

    private fun convertToPostResponse(post: Post): PostResponse {
        return PostResponse(
            id = post.id!!,
            title = post.title,
            content = post.content,
            status = post.status,
            author = AuthorResponse(
                id = post.author.id!!,
                name = post.author.name
            ),
            createdAt = post.createdAt!!,
            updatedAt = post.updatedAt!!,
            files = post.files.map { postFile ->
                FileResponse(
                    id = postFile.file.id!!,
                    originalName = postFile.file.originalName,
                    fileSize = postFile.file.fileSize,
                    contentType = postFile.file.contentType,
                    downloadUrl = "/api/files/${postFile.file.id}/download",
                    createdAt = postFile.file.createdAt!!
                )
            }
        )
    }
}