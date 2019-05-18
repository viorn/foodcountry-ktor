package com.taganhorn.routes.root

import com.taganhorn.security.Role
import com.taganhorn.security.authenticateRoles
import io.ktor.routing.Route

fun Route.priv()  {
    authenticateRoles(*Role.values()) {
        user()
        ingredient()
    }
}