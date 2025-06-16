package com.example.kotlinsample.common.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<String>? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "성공"): ApiResponse<T> {
            return ApiResponse(success = true, message = message, data = data)
        }

        fun <T> success(message: String = "성공"): ApiResponse<T> {
            return ApiResponse(success = true, message = message)
        }

        fun <T> error(message: String, errors: List<String>? = null): ApiResponse<T> {
            return ApiResponse(success = false, message = message, errors = errors)
        }
    }
}