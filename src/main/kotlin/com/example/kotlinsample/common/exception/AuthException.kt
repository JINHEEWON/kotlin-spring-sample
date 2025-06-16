package com.example.kotlinsample.common.exception

class InvalidCredentialsException(message: String) : RuntimeException(message)
class TokenExpiredException(message: String) : RuntimeException(message)
class InvalidTokenException(message: String) : RuntimeException(message)
