package com.taganhorn.interators

import com.taganhorn.kodein
import org.kodein.di.generic.instance

val CaloriesInteractor by kodein.instance<ICaloriesInteractor>()

interface ICaloriesInteractor {
    fun calculateCalorie(fats: Float, squirrels: Float, carbohydrates: Float): Float
}

class CaloriesInteractorImpl: ICaloriesInteractor {
    override fun calculateCalorie(fats: Float, squirrels: Float, carbohydrates: Float): Float {
        return fats*8+squirrels*4+carbohydrates*4
    }
}