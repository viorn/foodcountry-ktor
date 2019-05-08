package com.taganhorn.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.taganhorn.*
import com.taganhorn.repositories.UserRepository
import com.taganhorn.tools.permutate
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.routing.Route
import kotlinx.coroutines.runBlocking
import java.util.*

object JwtConfig {

    private val secret = "mo2sd-foodcountry-secret-hh34"
    private val issuer = "ktor.io:foodcountry"
    private val validityInMs = 60000
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Produce a token for this combination of User and Account
     */
    fun makeAuthToken(uuid: String, userId: Int, roles: List<String>): String {
        return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withClaim("id", userId)
            .withClaim("expires", getExpiration())
            .withClaim("roles", roles.joinToString(","))
            .withClaim("uuid", uuid)
            .sign(algorithm)
    }

    fun makeRefreshToken(uuid: String, userId: Int): String {
        return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withSubject("Refresh")
            .withIssuer(issuer)
            .withClaim("id", userId)
            .withClaim("uuid", uuid)
            .sign(algorithm)
    }

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)

}

data class AuthPrincipal(
    val userId: Int,
    val roles: List<Role>,
    val uuid: String
) : Principal

enum class Role {
    USER, ADMIN, SYSTEM
}

fun Array<out Role>.toConfigName(): String {
    return sortedBy { it.ordinal }.joinToString(separator = ",")
}

fun Route.authenticateRoles(vararg roles: Role, build: Route.() -> Unit) {
    authenticate(roles.toConfigName(), build = build)
}

fun Authentication.Configuration.mainApiJwtAuth(vararg roles: Role = Role.values()) {
    if (roles.isEmpty()) throw Throwable("roles is empty")
    val rolesSet = roles.permutate().map { it.sortedBy { it.ordinal } }.toSet()
    println("st3: $rolesSet")
    rolesSet.forEach { roles ->
        val rolesList = roles.toTypedArray().toConfigName()
        provider(rolesList) {
            this.skipWhen {
                runBlocking {
                    val token = it.request.header("Authorization")?.split(" ")?.get(1) ?: throw TokenIsEmptyException()
                    runCatching {
                        val jwt = JwtConfig.verifier.verify(token)
                        if (jwt.getClaim("expires").asDate().time < Date().time) throw TokenIsExpiresException()
                        val r = jwt.getClaim("roles").asString().split(",").map { Role.valueOf(it) }
                        val uuid = jwt.getClaim("uuid").asString()
                        if (!UserRepository.checkTokenUUID(uuid)) throw TokenIsLockException()
                        if (roles.any { r.contains(it) }) {
                            it.authentication.principal(AuthPrincipal(jwt.getClaim("id").asInt(), r, uuid))
                        } else {
                            throw RoleIsWrongException()
                        }
                    }.onFailure {
                        if (it is HttpStatusException) throw it
                        throw HttpStatusException(HttpStatusCode.InternalServerError, it.message!!)
                    }
                }
                return@skipWhen true
            }
        }
    }
}