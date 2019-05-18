package com.taganhorn

import com.taganhorn.repositories.DataBaseProvider
import com.taganhorn.routes.admin.admin
import com.taganhorn.routes.root.priv
import com.taganhorn.security.mainApiJwtAuth
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.features.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import io.ktor.auth.*
import io.ktor.gson.*
import routes.public.public

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    DataBaseProvider.init()

    install(Locations)

    install(AutoHeadResponse)

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(DataConversion)

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    // https://ktor.io/servers/features/https-redirect.html#testing
    /*if (!testing) {
        install(HttpsRedirect) {
            // The port to redirect to. By default 443, the default HTTPS port.
            sslPort = 443
            // 301 Moved Permanently, or 302 Found redirect.
            permanentRedirect = true
        }
    }*/

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(Authentication) {
        mainApiJwtAuth()
    }

    install(ContentNegotiation) {
        gson {
            excludeFieldsWithoutExposeAnnotation()
        }
    }

    install(StatusPages) {
        exception<HttpStatusException> {
            call.respond(
                status = it.statusCode,
                message = mapOf<String, Any>(
                    "message" to (it.message ?: ""),
                    "stackTrace" to it.stackTrace.map { it.toString() }
                )
            )
        }
    }

    routing {
        route("/") {
            priv()
            public()
            admin()
        }
        webSocket("/myws/echo") {
            send(Frame.Text("Hi from server"))
            while (true) {
                val frame = incoming.receive()
                if (frame is Frame.Text) {
                    send(Frame.Text("Client said: " + frame.readText()))
                }
            }
        }
    }
}

