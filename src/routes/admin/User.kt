package com.taganhorn.routes.admin

import com.taganhorn.UserNotFoundException
import com.taganhorn.repositories.UserRepository
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.get

fun Route.user() = route("/user") {
    @Location("/list/{page}")
    class ListLocation(val page: Int)
    get<ListLocation> {
        val limit = call.parameters["limit"]?.toInt() ?: 10
        call.respond(mapOf(
            "total" to UserRepository.totalUsers(),
            "list" to UserRepository.getUsers(it.page*limit, limit)
        ))
    }

    @Location("/lock/{id}")
    class LockUserLocation(val id: Int)
    put<LockUserLocation> {
        val id = it.id
        call.respond(mapOf(
            "user" to (UserRepository.lockUser(id) ?: throw UserNotFoundException())
        ))
    }

    @Location("/unlock/{id}")
    class UnLockUserLocation(val id: Int)
    put<UnLockUserLocation> {
        val id = it.id
        call.respond(mapOf(
            "user" to (UserRepository.unlockUser(id) ?: throw UserNotFoundException())
        ))
    }
}

