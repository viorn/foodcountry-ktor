package com.taganhorn.repositories

import com.taganhorn.entities.*
import com.taganhorn.kodein
import com.taganhorn.security.Role
import com.taganhorn.tables.DishesTable
import com.taganhorn.tables.DishesIngredientTable
import com.taganhorn.tools.sha1
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.generic.instance
import database.tables.IngredientsTable
import database.tables.TokenUUIDTable
import database.tables.UsersTable
import database.tables.UsersRoleTable

val DataBaseProvider by kodein.instance<IDataBaseProvider>()

interface IDataBaseProvider {
    fun init()
    val database: Database?
}

class DataBaseProviderImpl : IDataBaseProvider {
    override var database: Database? = null
        private set

    override fun init() = runBlocking {
        database = Database.connect(
            url = "jdbc:postgresql://db/foodcountry",
            driver = "org.postgresql.Driver",
            user = "foodcountry",
            password = "foodcountry"
        )
        transaction {
            SchemaUtils.apply {
                create(UsersTable)
                create(UsersRoleTable)
                create(TokenUUIDTable)
                create(IngredientsTable)
                create(DishesTable)
                create(DishesIngredientTable)
            }
        }
        var systemUser: User = UserRepository.findUserByName("SYSTEM") ?: run {
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
        if (IngredientRepository.totalIngredients() == 0) {
            val ingredientMap = IngredientRepository.addIngredient(
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
            ).map { it.name to it }.toMap()
            if (DishRepository.getDishesCount() == 0) {
                DishRepository.addDishes(Dish(
                    name = "Хлеб с яйцом",
                    ownerId = systemUser.id,
                    visible = VisibleType.PUBLIC,
                    ingredients = mapOf(
                        ingredientMap["Яйца куринные"]?.id!! to 200,
                        ingredientMap["Хлеб"]?.id!! to 200
                    )
                ))
            }
        }
    }
}