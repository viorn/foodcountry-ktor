package com.taganhorn

import io.ktor.http.HttpStatusCode

open class HttpStatusException(
    val statusCode: HttpStatusCode,
    message: String
): Exception(message)

class UserNotFoundException: HttpStatusException(HttpStatusCode.NotFound, "USER_NOT_FOUND")
class TokenIsEmptyException: HttpStatusException(HttpStatusCode.Unauthorized, "TOKEN_IS_EMPTY")
class TokenIsExpiresException: HttpStatusException(HttpStatusCode.Unauthorized, "TOKEN_IS_EXPIRES")
class TokenIsLockException: HttpStatusException(HttpStatusCode.Unauthorized, "TOKEN_IS_LOCK")
class RoleIsWrongException: HttpStatusException(HttpStatusCode.Unauthorized, "ROLE_IS_WRONG")
class TokenErrorException: HttpStatusException(HttpStatusCode.Unauthorized,"TOKEN_ERROR")