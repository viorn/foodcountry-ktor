package com.taganhorn.entities

import com.google.gson.annotations.Expose
import com.taganhorn.repositories.IngredientRepository

data class Dish(
    @Expose val id: Int = -1,
    @Expose val name: String,
    @Expose val ownerId: Int,
    @Expose val visible: VisibleType = VisibleType.PRIVATE,
    @Expose var ingredients: List<DishesIngredient>? = null
) {
    suspend fun loadIngredients() {
        ingredients = IngredientRepository.getIngredientsByDish(id)
    }

    constructor(
        name: String,
        ownerId: Int,
        visible: VisibleType,
        ingredients: Map<Int, Int>
    ) : this(
        name = name, ownerId = ownerId, visible = visible
    ) {
        this.ingredients = ingredients.map {
            DishesIngredient(
                ingredientId = it.key,
                weight = it.value
            )
        }
    }
}