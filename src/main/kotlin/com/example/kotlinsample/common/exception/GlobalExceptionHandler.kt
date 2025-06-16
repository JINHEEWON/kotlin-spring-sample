package com.example.kotlinsample.common.exception

import com.example.kotlinsample.common.exception.dto.ErrorDetail
import com.example.kotlinsample.common.exception.dto.ErrorResponse
import com.example.kotlinsample.common.exception.dto.FieldError
import com.example.kotlinsample.member.web.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

//    @ExceptionHandler(MethodArgumentNotValidException::class)
//    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
//        val errors = ex.bindingResult.fieldErrors
//            .associate { it.field to (it.defaultMessage ?: "잘못된 입력입니다") }
//
//        val message = errors.values.firstOrNull() ?: "유효성 검사 실패"
//        return ResponseEntity
//            .badRequest()
//            .body(ApiResponse.error(message))
//    }

    /**
     * 잘못된 인증 정보 예외 처리
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(
        ex: InvalidCredentialsException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "INVALID_CREDENTIALS",
                message = ex.message ?: "이메일 또는 비밀번호가 올바르지 않습니다"
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * 토큰 만료 예외 처리
     */
    @ExceptionHandler(TokenExpiredException::class)
    fun handleTokenExpiredException(
        ex: TokenExpiredException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "TOKEN_EXPIRED",
                message = ex.message ?: "토큰이 만료되었습니다"
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * 유효하지 않은 토큰 예외 처리
     */
    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(
        ex: InvalidTokenException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "INVALID_TOKEN",
                message = ex.message ?: "유효하지 않은 토큰입니다"
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "RESOURCE_NOT_FOUND",
                message = ex.message ?: "리소스를 찾을 수 없습니다"
            )
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "UNAUTHORIZED",
                message = ex.message ?: "인증이 필요합니다"
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(ForbiddenException::class, AccessDeniedException::class)
    fun handleForbidden(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "FORBIDDEN",
                message = ex.message ?: "접근 권한이 없습니다"
            )
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "BAD_REQUEST",
                message = ex.message ?: "잘못된 요청입니다"
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "CONFLICT",
                message = ex.message ?: "데이터 충돌이 발생했습니다"
            )
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(FileUploadException::class)
    fun handleFileUpload(ex: FileUploadException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "FILE_UPLOAD_ERROR",
                message = ex.message ?: "파일 업로드 중 오류가 발생했습니다"
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                message = error.defaultMessage ?: "유효하지 않은 값입니다"
            )
        }

        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "VALIDATION_ERROR",
                message = "입력 데이터가 올바르지 않습니다",
                details = fieldErrors
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(ex: BindException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                message = error.defaultMessage ?: "유효하지 않은 값입니다"
            )
        }

        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "VALIDATION_ERROR",
                message = "입력 데이터가 올바르지 않습니다",
                details = fieldErrors
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleCustomValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.errors.map { (field, message) ->
            FieldError(field = field, message = message)
        }

        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "VALIDATION_ERROR",
                message = ex.message ?: "유효성 검증에 실패했습니다",
                details = fieldErrors
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "TYPE_MISMATCH",
                message = "요청 파라미터 타입이 올바르지 않습니다: ${ex.name}"
            )
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace() // 로깅을 위해 스택 트레이스 출력

        val errorResponse = ErrorResponse(
            error = ErrorDetail(
                code = "INTERNAL_SERVER_ERROR",
                message = "서버 내부 오류가 발생했습니다"
            )
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
