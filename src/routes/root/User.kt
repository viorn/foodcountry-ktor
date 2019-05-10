package com.taganhorn.routes.root

import com.taganhorn.TokenErrorException
import com.taganhorn.repositories.UserRepository
import com.taganhorn.security.AuthPrincipal
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.route

fun Route.user() = route("/user") {
    get("/status") {
        val userId = call.principal<AuthPrincipal>()?.userId ?: throw TokenErrorException()
        call.respond(
            mapOf(
                "user" to UserRepository.getUser(userId)
            )
        )
    }
    get("/logout") {
        UserRepository.deleteTokenUUID(call.principal<AuthPrincipal>()!!.uuid)
        call.respond(HttpStatusCode.OK)
    }
}