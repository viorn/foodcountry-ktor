package com.taganhorn

import com.taganhorn.interators.CaloriesInteractorImpl
import com.taganhorn.interators.ICaloriesInteractor
import com.taganhorn.repositories.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

val kodein = Kodein {
    bind<IDataBaseProvider>() with singleton { DataBaseProviderImpl() }
    bind<IUserRepository>() with singleton { UserRepositoryImpl() }
    bind<IIngredientRepository>() with singleton { IngredientRepositoryImpl() }

    bind<ICaloriesInteractor>() with singleton { CaloriesInteractorImpl() }
}