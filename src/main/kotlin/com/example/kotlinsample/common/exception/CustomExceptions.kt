package com.example.kotlinsample.common.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)

class ForbiddenException(message: String) : RuntimeException(message)

class FileUploadException(message: String) : RuntimeException(message)

class ValidationException(message: String, val errors: Map<String, String>) : RuntimeException(message)