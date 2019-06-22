package com.taganhorn.entities

import com.google.gson.annotations.Expose

data class DishesIngredient(
    @Expose val ingredientId: Int,
    @Expose val weight: Int
) {
    var ingredient: Ingredient? = null
}