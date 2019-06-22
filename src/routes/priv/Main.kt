package com.taganhorn.routes.root

import com.taganhorn.routes.priv.dishes
import com.taganhorn.security.Role
import com.taganhorn.security.authenticateRoles
import io.ktor.routing.Route

fun Route.priv()  {
    authenticateRoles(*Role.values()) {
        user()
        ingredient()
        dishes()
    }
}