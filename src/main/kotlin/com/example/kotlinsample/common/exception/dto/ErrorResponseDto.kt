package com.example.kotlinsample.common.exception.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * API 오류 응답 최상위 클래스
 * 모든 API 오류 응답의 통일된 형식을 제공합니다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 오류 상세 정보
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: List<Any>? = null,
    val path: String? = null,
    val method: String? = null
)

/**
 * 필드 검증 오류 정보
 * @Valid 어노테이션으로 발생하는 validation 오류에 사용
 */
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)

/**
 * 비즈니스 로직 검증 오류 정보
 * 커스텀 비즈니스 규칙 위반 시 사용
 */
data class BusinessError(
    val rule: String,
    val message: String,
    val context: Map<String, Any>? = null
)

/**
 * 외부 API 호출 오류 정보
 * 외부 서비스 연동 실패 시 사용
 */
data class ExternalApiError(
    val service: String,
    val endpoint: String,
    val statusCode: Int,
    val message: String
)

/**
 * API 응답 생성을 위한 Builder 클래스
 * 다양한 오류 상황에 맞는 ErrorResponse를 쉽게 생성할 수 있습니다.
 */
class ErrorResponseBuilder {

    companion object {

        /**
         * 단순 오류 응답 생성
         */
        fun simple(code: String, message: String): ErrorResponse {
            return ErrorResponse(
                error = ErrorDetail(
                    code = code,
                    message = message
                )
            )
        }

        /**
         * Validation 오류 응답 생성
         */
        fun validation(
            message: String = "입력 데이터 검증에 실패했습니다",
            fieldErrors: List<FieldError>
        ): ErrorResponse {
            return ErrorResponse(
                error = ErrorDetail(
                    code = "VALIDATION_ERROR",
                    message = message,
                    details = fieldErrors
                )
            )
        }

        /**
         * 비즈니스 로직 오류 응답 생성
         */
        fun business(
            code: String,
            message: String,
            businessErrors: List<BusinessError>? = null
        ): ErrorResponse {
            return ErrorResponse(
                error = ErrorDetail(
                    code = code,
                    message = message,
                    details = businessErrors
                )
            )
        }

        /**
         * 외부 API 오류 응답 생성
         */
        fun externalApi(
            message: String = "외부 서비스 연동 중 오류가 발생했습니다",
            externalApiError: ExternalApiError
        ): ErrorResponse {
            return ErrorResponse(
                error = ErrorDetail(
                    code = "EXTERNAL_API_ERROR",
                    message = message,
                    details = listOf(externalApiError)
                )
            )
        }

        /**
         * HTTP 요청 정보를 포함한 오류 응답 생성
         */
        fun withRequestInfo(
            code: String,
            message: String,
            path: String? = null,
            method: String? = null,
            details: List<Any>? = null
        ): ErrorResponse {
            return ErrorResponse(
                error = ErrorDetail(
                    code = code,
                    message = message,
                    details = details,
                    path = path,
                    method = method
                )
            )
        }
    }
}

/**
 * 성공 응답을 위한 공통 클래스
 * 오류 응답과 형식을 맞추기 위해 제공
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SuccessResponse<T>(
    val success: Boolean = true,
    val data: T,
    val message: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now()
)