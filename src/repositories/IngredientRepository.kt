package com.taganhorn.repositories

import com.taganhorn.entities.Ingredient
import com.taganhorn.entities.VisibleType
import com.taganhorn.kodein
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.generic.instance
import tables.Ingredients

val IngredientRepository by kodein.instance<IIngredientRepository>()

interface IIngredientRepository {
    suspend fun getIngredients(offset: Int = 0, limit: Int = 10, userIds: List<Int>? = null): List<Ingredient>

    suspend fun addIngredient(vararg ingredients: Ingredient): List<Ingredient>

    suspend fun editIngredient(ingredient: Ingredient, userId: Int? = null): Ingredient

    suspend fun deleteIngredient(id: Int, userId: Int? = null): Int

    suspend fun totalIngredients(userIds: List<Int>? = null): Int
}

class IngredientRepositoryImpl : IIngredientRepository {

    init {
        transaction {
            SchemaUtils.apply {
                create(Ingredients)
            }
        }
    }

    override suspend fun getIngredients(offset: Int, limit: Int, userIds: List<Int>?): List<Ingredient> =
        withContext(Dispatchers.IO) {
            transaction {
                return@transaction Ingredients.select {
                    if (userIds.isNullOrEmpty()) OP_TRUE else Ingredients.ownerId.inList(userIds)
                }.orderBy(Ingredients.id, SortOrder.DESC).limit(limit, offset).map {
                    Ingredient(
                        id = it[Ingredients.id],
                        name = it[Ingredients.name],
                        fats = it[Ingredients.fats],
                        squirrels = it[Ingredients.squirrels],
                        carbohydrates = it[Ingredients.carbohydrates],
                        ownerId = it[Ingredients.ownerId],
                        visible = VisibleType.valueOf(it[Ingredients.visible])
                    )
                }
            }
        }

    override suspend fun addIngredient(vararg ingredients: Ingredient) = withContext(Dispatchers.IO) {
        transaction {
            Ingredients.batchInsert(ingredients.asIterable()) {
                this[Ingredients.name] = it.name
                this[Ingredients.fats] = it.fats
                this[Ingredients.carbohydrates] = it.carbohydrates
                this[Ingredients.squirrels] = it.squirrels
                this[Ingredients.ownerId] = it.ownerId!!
                this[Ingredients.visible] = it.visible.name
            }
        }.map {
            Ingredient(
                id = it[Ingredients.id],
                name = it[Ingredients.name],
                fats = it[Ingredients.fats],
                carbohydrates = it[Ingredients.carbohydrates],
                squirrels = it[Ingredients.squirrels],
                ownerId = it[Ingredients.ownerId],
                visible = it[Ingredients.visible].let { VisibleType.valueOf(it) }
            )
        }
    }

    override suspend fun editIngredient(ingredient: Ingredient, userId: Int?) = withContext(Dispatchers.IO) {
        transaction {
            Ingredients.update({
                (Ingredients.id eq ingredient.id) and (if (userId == null) OP_TRUE else Ingredients.ownerId eq userId)
            }) {
                it[Ingredients.name] = ingredient.name
                it[Ingredients.fats] = ingredient.fats
                it[Ingredients.carbohydrates] = ingredient.carbohydrates
                it[Ingredients.squirrels] = ingredient.squirrels
                it[Ingredients.visible] = ingredient.visible.name
            }
            return@transaction ingredient
        }
    }

    override suspend fun deleteIngredient(id: Int, userId: Int?) = withContext(Dispatchers.IO) {
        transaction {
            Ingredients.deleteWhere {
                (Ingredients.id eq id) and
                        (if (userId == null) OP_TRUE else Ingredients.ownerId eq userId)
            }
        }
    }

    override suspend fun totalIngredients(userIds: List<Int>?): Int = withContext(Dispatchers.IO) {
        transaction {
            Ingredients.select {
                if (userIds.isNullOrEmpty()) OP_TRUE else Ingredients.ownerId.inList(userIds)
            }.count()
        }
    }
}