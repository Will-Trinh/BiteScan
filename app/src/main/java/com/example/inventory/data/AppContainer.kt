/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.data
import com.example.inventory.ui.AppViewModel
import android.content.Context
import com.example.inventory.ui.settings.MyPantryViewModel
/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val itemsRepository: ItemsRepository
    val receiptsRepository: ReceiptsRepository
    val recipesRepository: RecipesRepository
    val usersRepository: UsersRepository
    val onlineReceiptsRepository: OnlineReceiptsRepository
    val onlineRecipesRepository: OnlineRecipesRepository
    val onlineUsersRepository: OnlineUsersRepository
    val myPantryViewModel: MyPantryViewModel

}

/**
 * [AppContainer] implementation that provides instances of offline repositories
 */

class AppDataContainer(private val context: Context) : AppContainer {

    private val database: InventoryDatabase by lazy {
        InventoryDatabase.getDatabase(context)
    }

    // 1. Items Repository
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(database.itemDao())
    }

    // 2.AppViewModel instance
    private val appViewModel: AppViewModel by lazy {
        AppViewModel()
    }

    // 3. OnlineReceiptsRepository
    override val onlineReceiptsRepository: OnlineReceiptsRepository by lazy {
        OnlineReceiptsRepository.create(
            receiptsRepository = OfflineReceiptsRepository(database.receiptDao()),
            itemsRepository = itemsRepository
        )
    }

    // 4. ReceiptsRepository (sync + check internet)
    override val receiptsRepository: ReceiptsRepository by lazy {
        ReceiptsRepositoryImpl(
            offlineRepo = OfflineReceiptsRepository(database.receiptDao()),
            onlineRepo = onlineReceiptsRepository,
            context = context
        )
    }

    // 5. RecipesRepository
    override val recipesRepository: RecipesRepository by lazy {
        OfflineRecipesRepository(database.recipeDao())
    }
    override val onlineRecipesRepository: OnlineRecipesRepository by lazy {
        OnlineRecipesRepository.create(
            recipesRepository = OfflineRecipesRepository(database.recipeDao()),
            itemsRepository = itemsRepository
        )
    }
    // 6. MyPantryViewModel
    override val myPantryViewModel: MyPantryViewModel by lazy {
        MyPantryViewModel(itemsRepository, receiptsRepository)
    }
    // 7. UsersRepository
    override val usersRepository: UsersRepository by lazy {
        OfflineUsersRepository(
            userDao = database.userDao(),
            itemDao = database.itemDao(),
            receiptDao = database.receiptDao(),
            recipeDao = database.recipeDao()
        )
    }
    override val onlineUsersRepository: OnlineUsersRepository by lazy {
        OnlineUsersRepository(
            userDao = database.userDao()
        )
    }
}