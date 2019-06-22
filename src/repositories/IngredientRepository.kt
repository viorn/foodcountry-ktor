package com.taganhorn.repositories

import com.taganhorn.entities.DishesIngredient
import com.taganhorn.entities.Ingredient
import com.taganhorn.entities.VisibleType
import com.taganhorn.kodein
import com.taganhorn.tables.DishesIngredientTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.generic.instance
import database.tables.IngredientsTable

val IngredientRepository by kodein.instance<IIngredientRepository>()

interface IIngredientRepository {
    suspend fun getIngredients(offset: Int = 0, limit: Int = 10, userIds: List<Int>? = null): List<Ingredient>

    suspend fun getIngredientsByDish(dishId: Int): List<DishesIngredient>

    suspend fun addIngredient(vararg ingredients: Ingredient): List<Ingredient>

    suspend fun editIngredient(ingredient: Ingredient, userId: Int? = null): Ingredient

    suspend fun deleteIngredient(id: Int, userId: Int? = null): Int

    suspend fun totalIngredients(userIds: List<Int>? = null): Int
}

class IngredientRepositoryImpl : IIngredientRepository {
    override suspend fun getIngredientsByDish(dishId: Int): List<DishesIngredient> = withContext(Dispatchers.IO) {
        transaction {
            val tmpData = DishesIngredientTable.select {
                DishesIngredientTable.dishId eq dishId
            }.map {
                it[DishesIngredientTable.ingredientId] to it[DishesIngredientTable.weight]
            }
            val ingredients = HashMap<Int, Ingredient>()
            IngredientsTable.select {
                IngredientsTable.id.inList(tmpData.map { it.first })
            }.forEach {
                ingredients[it[IngredientsTable.id]] = Ingredient(
                    id = it[IngredientsTable.id],
                    name = it[IngredientsTable.name],
                    fats = it[IngredientsTable.fats],
                    squirrels = it[IngredientsTable.squirrels],
                    carbohydrates = it[IngredientsTable.carbohydrates],
                    ownerId = it[IngredientsTable.ownerId],
                    visible = VisibleType.valueOf(it[IngredientsTable.visible])
                )
            }
            return@transaction tmpData.map {
                DishesIngredient(
                    ingredientId = it.first,
                    weight = it.second
                ).apply {
                    this.ingredient = ingredients[it.first]!!
                }
            }
        }
    }

    override suspend fun getIngredients(offset: Int, limit: Int, userIds: List<Int>?): List<Ingredient> =
        withContext(Dispatchers.IO) {
            transaction {
                return@transaction IngredientsTable.select {
                    if (userIds.isNullOrEmpty()) OP_TRUE else IngredientsTable.ownerId.inList(userIds)
                }.orderBy(IngredientsTable.id, SortOrder.DESC).limit(limit, offset).map {
                    Ingredient(
                        id = it[IngredientsTable.id],
                        name = it[IngredientsTable.name],
                        fats = it[IngredientsTable.fats],
                        squirrels = it[IngredientsTable.squirrels],
                        carbohydrates = it[IngredientsTable.carbohydrates],
                        ownerId = it[IngredientsTable.ownerId],
                        visible = VisibleType.valueOf(it[IngredientsTable.visible])
                    )
                }
            }
        }

    override suspend fun addIngredient(vararg ingredients: Ingredient) = withContext(Dispatchers.IO) {
        transaction {
            IngredientsTable.batchInsert(ingredients.asIterable()) {
                this[IngredientsTable.name] = it.name
                this[IngredientsTable.fats] = it.fats
                this[IngredientsTable.carbohydrates] = it.carbohydrates
                this[IngredientsTable.squirrels] = it.squirrels
                this[IngredientsTable.ownerId] = it.ownerId!!
                this[IngredientsTable.visible] = it.visible.name
            }
        }.map {
            Ingredient(
                id = it[IngredientsTable.id],
                name = it[IngredientsTable.name],
                fats = it[IngredientsTable.fats],
                carbohydrates = it[IngredientsTable.carbohydrates],
                squirrels = it[IngredientsTable.squirrels],
                ownerId = it[IngredientsTable.ownerId],
                visible = it[IngredientsTable.visible].let { VisibleType.valueOf(it) }
            )
        }
    }

    override suspend fun editIngredient(ingredient: Ingredient, userId: Int?) = withContext(Dispatchers.IO) {
        transaction {
            IngredientsTable.update({
                (IngredientsTable.id eq ingredient.id) and (if (userId == null) OP_TRUE else IngredientsTable.ownerId eq userId)
            }) {
                it[IngredientsTable.name] = ingredient.name
                it[IngredientsTable.fats] = ingredient.fats
                it[IngredientsTable.carbohydrates] = ingredient.carbohydrates
                it[IngredientsTable.squirrels] = ingredient.squirrels
                it[IngredientsTable.visible] = ingredient.visible.name
            }
            return@transaction ingredient
        }
    }

    override suspend fun deleteIngredient(id: Int, userId: Int?) = withContext(Dispatchers.IO) {
        transaction {
            IngredientsTable.deleteWhere {
                (IngredientsTable.id eq id) and
                        (if (userId == null) OP_TRUE else IngredientsTable.ownerId eq userId)
            }
        }
    }

    override suspend fun totalIngredients(userIds: List<Int>?): Int = withContext(Dispatchers.IO) {
        transaction {
            IngredientsTable.select {
                if (userIds.isNullOrEmpty()) OP_TRUE else IngredientsTable.ownerId.inList(userIds)
            }.count()
        }
    }
}