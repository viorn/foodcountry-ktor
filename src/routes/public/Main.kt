package routes.public

import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.public() = route("/public") {
    user()
}