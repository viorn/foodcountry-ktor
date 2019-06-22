package com.taganhorn.routes.priv

import com.taganhorn.repositories.DishRepository
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.dishes() = route("/dish") {
    @Location("/list/{page}")
    class ListLocation(val page: Int)
    get<ListLocation> {
        val limit = call.parameters["limit"]?.toInt() ?: 10
        /*val principal = call.principal<AuthPrincipal>()
        var users: List<Int>? = listOfNotNull(principal?.userId)
        if (principal?.roles?.contains(Role.ADMIN) == true || principal?.roles?.contains(Role.SYSTEM) == true) {
            users = call.parameters["users"]?.split(",")?.map { it.toInt() }
        }*/
        call.respond(
            mapOf(
                "list" to DishRepository.getDishes(offset = limit * it.page, limit = limit).apply {
                    forEach {
                        it.loadIngredients()
                    }
                },
                "total" to DishRepository.getDishesCount()
            )
        )
    }
}