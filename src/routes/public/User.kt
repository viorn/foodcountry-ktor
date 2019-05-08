package routes.public

import com.taganhorn.HttpStatusException
import com.taganhorn.TokenIsLockException
import com.taganhorn.entities.User
import com.taganhorn.repositories.UserRepository
import com.taganhorn.security.JwtConfig
import com.taganhorn.security.Role
import com.taganhorn.tools.sha1
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import java.util.*

fun Route.user() = route("/user") {
    post("/auth") {
        val request = call.receive<Map<String, Any>>()
        val name = request["name"] as? String ?: throw HttpStatusException(HttpStatusCode.BadRequest, "NAME_ERROR")
        val password =
            request["password"] as? String ?: throw HttpStatusException(HttpStatusCode.BadRequest, "PASSWORD_ERROR")
        val user =
            UserRepository.findUserByName(name) ?: throw HttpStatusException(
                HttpStatusCode.BadRequest,
                "USER_NOT_FOUND"
            )
        if (user.password == password.sha1()) {
            val uuid = UUID.randomUUID().toString()
            val authToken = JwtConfig.makeAuthToken(uuid, user.id, user.roles.map { it.name })
            val refreshToken = JwtConfig.makeRefreshToken(uuid, user.id)
            UserRepository.addTokenUUID(user.id, uuid, call.request.header("User-Agent"))
            call.respond(
                mapOf(
                    "user" to user,
                    "authToken" to authToken,
                    "refreshToken" to refreshToken
                )
            )
        } else {
            throw HttpStatusException(HttpStatusCode.BadRequest, "PASSWORD_IS_WRONG")
        }
    }

    post("/refresh") {
        val request = call.receive<Map<String, Any>>()
        val authToken =
            request["authToken"] as? String ?: throw HttpStatusException(HttpStatusCode.BadRequest, "TOKEN_ERROR")
        val authDecodeResult = JwtConfig.verifier.verify(authToken)
        val authUUID = authDecodeResult.getClaim("uuid").asString()
        val authUserId = authDecodeResult.getClaim("id").asInt()
        val refreshToken =
            request["refreshToken"] as? String ?: throw HttpStatusException(HttpStatusCode.BadRequest, "TOKEN_ERROR")
        val refreshDecodeResult = JwtConfig.verifier.verify(refreshToken)
        val refreshUUID = refreshDecodeResult.getClaim("uuid").asString()
        val refreshUserId = refreshDecodeResult.getClaim("id").asInt()
        if (authUUID == refreshUUID && authUserId == refreshUserId) {
            val user = UserRepository.getUser(authUserId) ?: throw HttpStatusException(
                HttpStatusCode.BadRequest,
                "USER_NOT_FOUND"
            )
            val newUUID = UUID.randomUUID().toString()
            val newAuthToken = JwtConfig.makeAuthToken(newUUID, user.id, user.roles.map { it.name })
            val newRefreshToken = JwtConfig.makeRefreshToken(newUUID, user.id)
            if (!UserRepository.checkTokenUUID(refreshUUID)) throw TokenIsLockException()
            UserRepository.deleteTokenUUID(refreshUUID)
            UserRepository.addTokenUUID(user.id, newUUID, call.request.header("User-Agent"))
            call.respond(
                mapOf(
                    "user" to user,
                    "authToken" to newAuthToken,
                    "refreshToken" to newRefreshToken
                )
            )
        } else {
            throw HttpStatusException(HttpStatusCode.BadRequest, "TOKEN_ERROR")
        }
    }

    post("/registration") {
        val request = call.receive<Map<String, Any>>()
        val name =
            (request["name"] as? String) ?: throw HttpStatusException(HttpStatusCode.BadRequest, "USERNAME_ERROR")
        val password =
            (request["password"] as? String) ?: throw HttpStatusException(HttpStatusCode.BadRequest, "PASSWORD_ERROR")
        if (name.isBlank()) throw HttpStatusException(HttpStatusCode.BadRequest, "USERNAME_IS_BLANK")
        if (password.isBlank()) throw HttpStatusException(HttpStatusCode.BadRequest, "PASSWORD_IS_BLANK")
        var user = UserRepository.findUserByName(name)
        if (user != null) throw HttpStatusException(HttpStatusCode.BadRequest, "SUCH_USER_EXISTS")
        user = UserRepository.addUser(
            User(
                name = name,
                password = password.sha1(),
                roles = listOf(Role.USER)
            )
        )
        call.respond(
            mapOf(
                "user" to user
            )
        )
    }
}