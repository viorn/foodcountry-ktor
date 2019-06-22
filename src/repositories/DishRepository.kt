package com.taganhorn.repositories

import com.taganhorn.entities.Dish
import com.taganhorn.entities.VisibleType
import com.taganhorn.kodein
import com.taganhorn.tables.DishesIngredientTable
import com.taganhorn.tables.DishesTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.generic.instance

val DishRepository by kodein.instance<IDishRepository>()

interface IDishRepository {
    suspend fun getDishes(offset: Int = 0, limit: Int = 10, ownerId: Int? = null): List<Dish>
    suspend fun addDishes(vararg dish: Dish): List<Dish>
    suspend fun getDishesCount(): Int
}

class DishRepositoryImpl : IDishRepository {
    override suspend fun getDishesCount(): Int = withContext(Dispatchers.IO) {
        transaction {
            DishesTable.selectAll().count()
        }
    }

    override suspend fun addDishes(vararg dishes: Dish): List<Dish> = withContext(Dispatchers.IO) {
        transaction {
            val dishes = dishes.map { dish ->
                DishesTable.insert {
                    it[DishesTable.name] = dish.name
                    it[DishesTable.ownerId] = dish.ownerId
                    it[DishesTable.visible] = dish.visible.name
                }.let {
                    dish.copy(
                        id = it[DishesTable.id]
                    )
                }
            }
            class TmpData(
                val dishId: Int,
                val ingredientId: Int,
                val weight: Int
            )
            DishesIngredientTable.batchInsert(dishes.flatMap { dish ->
                val dishId = dish.id
                dish.ingredients?.map {
                    TmpData(
                        dishId = dishId,
                        ingredientId = it.ingredientId,
                        weight = it.weight
                    )
                } ?: emptyList()
            }) {
                this[DishesIngredientTable.dishId] = it.dishId
                this[DishesIngredientTable.ingredientId] = it.ingredientId
                this[DishesIngredientTable.weight] = it.weight
            }
            return@transaction dishes
        }
    }

    override suspend fun getDishes(offset: Int, limit: Int, ownerId: Int?): List<Dish> = withContext(Dispatchers.IO) {
        return@withContext transaction {
            return@transaction DishesTable.select {
                if (ownerId == null) OP_TRUE else DishesTable.ownerId eq ownerId
            }.limit(limit, offset).map {
                Dish(
                    id = it[DishesTable.id],
                    name = it[DishesTable.name],
                    ownerId = it[DishesTable.ownerId],
                    visible = it[DishesTable.visible].let { VisibleType.valueOf(it) }
                )
            }
        }
    }
}