package com.taganhorn.routes.admin

import com.taganhorn.repositories.UserRepository
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.user() = route("/user") {
    get("/list") {
        call.respond(mapOf(
            "total" to UserRepository.usersCount(),
            "list" to UserRepository.getUsers()
        ))
    }
}