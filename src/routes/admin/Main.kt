package com.taganhorn.routes.admin

import com.taganhorn.security.Role
import com.taganhorn.security.authenticateRoles
import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.admin() = route("/admin") {
    authenticateRoles(Role.ADMIN, Role.SYSTEM) {
        user()
    }
}