package com.taganhorn.repositories

import com.taganhorn.entities.User
import com.taganhorn.security.Role
import com.taganhorn.tools.sha1
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database

object DataBaseProvider {
    var database: Database? = null
    fun init() = runBlocking {
        database = Database.connect(
            url = "jdbc:postgresql://db/foodcountry",
            driver = "org.postgresql.Driver",
            user = "foodcountry",
            password = "foodcountry"
        )
        if (UserRepository.usersCount() == 0) {
            UserRepository.addUser(
                User(
                    name = "SYSTEM",
                    password = "qwerty1989".sha1(),
                    roles = listOf(Role.SYSTEM, Role.USER, Role.ADMIN)
                )
            )
            //FOD DEBUG
            (0..10).forEach {
                UserRepository.addUser(
                    User(
                        name = "ADMIN$it",
                        password = "$it".sha1(),
                        roles = listOf(Role.USER, Role.ADMIN)
                    )
                )
            }
            (11..100).forEach {
                UserRepository.addUser(
                    User(
                        name = "USER$it",
                        password = "$it".sha1(),
                        roles = listOf(Role.USER)
                    )
                )
            }
            //
        }
    }
}