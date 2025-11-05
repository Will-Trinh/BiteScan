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

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val itemsRepository: ItemsRepository
    val receiptsRepository: ReceiptsRepository
    val recipesRepository: RecipesRepository
    val usersRepository: UsersRepository
}

/**
 * [AppContainer] implementation that provides instances of offline repositories
 */
class AppDataContainer(private val context: Context) : AppContainer {
    private val database: InventoryDatabase by lazy {
        InventoryDatabase.getDatabase(context)
    }
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(InventoryDatabase.getDatabase(context).itemDao())
    }

    override val receiptsRepository: ReceiptsRepository by lazy {
        ReceiptsRepositoryImpl(
            OfflineReceiptsRepository(InventoryDatabase.getDatabase(context).receiptDao()),
            OnlineReceiptsRepository.create()
        )
    }

    override val recipesRepository: RecipesRepository by lazy {
        OfflineRecipesRepository(InventoryDatabase.getDatabase(context).recipeDao())
    }

    override val usersRepository: UsersRepository by lazy {
        OfflineUsersRepository(
            userDao = database.userDao(),
            itemDao = database.itemDao(),
            receiptDao = database.receiptDao(),
            recipeDao = database.recipeDao()
        )
    }
}