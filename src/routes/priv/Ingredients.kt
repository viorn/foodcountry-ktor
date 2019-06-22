package com.taganhorn.routes.root

import com.google.gson.annotations.Expose
import com.taganhorn.entities.Ingredient
import com.taganhorn.repositories.IngredientRepository
import com.taganhorn.security.AuthPrincipal
import com.taganhorn.security.Role
import com.taganhorn.tools.createMap
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

data class AddIngredientRequest(
    @Expose val ingredient: Ingredient
)

fun Route.ingredient() = route("/ingredient") {
    @Location("/list/{page}")
    class ListLocation(val page: Int)
    get<ListLocation> {
        val limit = call.parameters["limit"]?.toInt() ?: 10
        val principal = call.principal<AuthPrincipal>()
        var users: List<Int>? = listOfNotNull(principal?.userId)
        if (principal?.roles?.contains(Role.ADMIN) == true || principal?.roles?.contains(Role.SYSTEM) == true) {
            users = call.parameters["users"]?.split(",")?.map { it.toInt() }
        }
        call.respond(
            mapOf(
                "list" to IngredientRepository.getIngredients(offset = limit * it.page, limit = limit, userIds = users),
                "total" to IngredientRepository.totalIngredients(userIds = users)
            )
        )
    }

    post("/add") {
        val principal = call.principal<AuthPrincipal>()
        val request = call.receive<AddIngredientRequest>()
        println(request.ingredient)
        call.respond(
            mapOf(
                "ingredient" to IngredientRepository.addIngredient(
                    request.ingredient.copy(
                        ownerId = principal!!.userId
                    )
                ).first()
            )
        )
    }

    @Location("/edit/{id}")
    class EditLocation(val id: Int)
    post<EditLocation> {
        val principal = call.principal<AuthPrincipal>()
        val request = call.receive<AddIngredientRequest>()
        call.respond(
            mapOf(
                "ingredient" to IngredientRepository.editIngredient(
                    request.ingredient.copy(
                        id = it.id
                    ),
                    principal!!.userId
                )
            )
        )
    }

    @Location("/{id}")
    class RemoveLocation(val id: Int)
    delete<RemoveLocation> {
        val principal = call.principal<AuthPrincipal>()
        val userId = principal!!.userId
        val roles = principal!!.roles
        IngredientRepository.deleteIngredient(
            it.id,
            if (roles.contains(Role.ADMIN) || roles.contains(Role.SYSTEM)) null else userId
        )
        call.respond(
            mapOf(
                "status" to "ok"
            )
        )
    }
}