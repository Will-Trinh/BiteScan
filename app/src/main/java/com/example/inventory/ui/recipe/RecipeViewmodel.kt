/*
- Recipe Viewmodel receive the data from the api and save to client data
- Recipe viewmodel will check the recipe from server to make sure the recipe is in the correct form.
- Recipe viewmodel will update recipe screen with the overview of the valid recipe.
- Recipe viewmodel need to get data from receipt such as item inside and receipt ID, also it should have syn function to update the items for use in the receipt.
- Also, recipe viewmodel should have a link to account to show limited recipe for type of user.
*/

package com.example.inventory.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.OfflineUsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.min
import com.example.inventory.R

