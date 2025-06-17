package com.example.kotlinsample.post.domain.model

import com.example.kotlinsample.file.domain.model.PostFile
import com.example.kotlinsample.user.domain.model.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener::class)
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var content: String?,

    @Column(name = "author_email", nullable = false)
    var authorEmail: String,  // 실제 데이터는 이 컬럼에 저장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_email", referencedColumnName = "email", insertable = false, updatable = false)
    val author: User? = null,  // email로 조인

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PostStatus = PostStatus.DRAFT,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column
    var deletedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val files: MutableList<PostFile> = mutableListOf()
){
    @PrePersist  // 저장되기 직전에 실행
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate   // 업데이트되기 직전에 실행
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

enum class PostStatus {
    DRAFT, PUBLISHED, ARCHIVED
}