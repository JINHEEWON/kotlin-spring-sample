package com.example.kotlinsample.post.web

import com.example.kotlinsample.member.web.ApiResponse
import com.example.kotlinsample.post.domain.dto.PostListResponse
import com.example.kotlinsample.post.domain.dto.PostRequest
import com.example.kotlinsample.post.domain.dto.PostResponse
import com.example.kotlinsample.post.domain.dto.PostUpdateRequest
import com.example.kotlinsample.post.domain.model.PostStatus
import com.example.kotlinsample.post.domain.service.PostService
import com.example.kotlinsample.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {

    /**
     * 게시글 목록 조회 (공개된 게시글만, 인증 불필요)
     */
    @GetMapping
    fun getPublishedPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<PostListResponse>> {
        val posts = postService.getPublishedPosts(page, size, search)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = posts,
                message = "게시글 목록을 조회했습니다"
            )
        )
    }

    /**
     * 전체 게시글 목록 조회 (관리자용, 모든 상태 포함)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sort: String,
        @RequestParam(defaultValue = "desc") direction: String,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) authorId: Long?,
        @RequestParam(required = false) status: PostStatus?
    ): ResponseEntity<ApiResponse<PostListResponse>> {
        val posts = postService.getAllPosts(page, size, sort, direction, search, authorId, status)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = posts,
                message = "전체 게시글 목록을 조회했습니다"
            )
        )
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{id}")
    fun getPostById(@PathVariable id: Long): ResponseEntity<ApiResponse<PostResponse>> {
        val post = postService.getPostById(id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = post,
                message = "게시글을 조회했습니다"
            )
        )
    }

    /**
     * 내 게시글 목록 조회
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun getMyPosts(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) status: PostStatus?
    ): ResponseEntity<ApiResponse<PostListResponse>> {
        val posts = postService.getMyPosts(userPrincipal.id, page, size, status)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = posts,
                message = "내 게시글 목록을 조회했습니다"
            )
        )
    }

    /**
     * 게시글 검색
     */
    @GetMapping("/search")
    fun searchPosts(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "true") publishedOnly: Boolean
    ): ResponseEntity<ApiResponse<PostListResponse>> {
        val posts = postService.searchPosts(keyword, page, size, publishedOnly)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = posts,
                message = "게시글 검색 결과입니다"
            )
        )
    }

    /**
     * 게시글 작성
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun createPost(
        @Valid @RequestBody request: PostRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<PostResponse>> {
        val post = postService.createPost(request, userPrincipal.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                data = post,
                message = "게시글이 생성되었습니다"
            )
        )
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun updatePost(
        @PathVariable id: Long,
        @Valid @RequestBody request: PostUpdateRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<PostResponse>> {
        val post = postService.updatePost(id, request, userPrincipal.id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = post,
                message = "게시글이 수정되었습니다"
            )
        )
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun deletePost(
        @PathVariable id: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Nothing>> {
        postService.deletePost(id, userPrincipal.id)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = null,
                message = "게시글이 삭제되었습니다"
            )
        )
    }
}