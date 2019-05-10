package com.taganhorn.repositories

import com.taganhorn.entities.Ingredient
import com.taganhorn.entities.User
import com.taganhorn.entities.VisibleType
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
        var systemUser: User = UserRepository.findUserByName("SYSTEM") ?: run{
            UserRepository.addUser(
                User(
                    name = "SYSTEM",
                    password = "qwerty1989".sha1(),
                    roles = listOf(Role.SYSTEM, Role.USER, Role.ADMIN)
                )
            ).apply {
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
            }
        }
        if (IngredientRepository.totalIngredients()==0) {
            IngredientRepository.addIngredient(
                Ingredient(
                    name = "Вода",
                    ownerId = systemUser.id,
                    visible = VisibleType.PUBLIC
                ),
                Ingredient(
                    name = "Яйца куринные",
                    ownerId = systemUser.id,
                    squirrels = 13f,
                    fats = 11f,
                    carbohydrates = 1f,
                    visible = VisibleType.PUBLIC
                ),
                Ingredient(
                    name = "Хлеб",
                    ownerId = systemUser.id,
                    squirrels = 8f,
                    fats = 1f,
                    carbohydrates = 49f,
                    visible = VisibleType.PUBLIC
                )
            )
        }
    }
}