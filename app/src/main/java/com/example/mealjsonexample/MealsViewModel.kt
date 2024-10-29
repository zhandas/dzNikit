package com.example.mealjsonexample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealsViewModel : ViewModel() {
    private var mealsRepository = MealsRepository()

    private var categories = MutableStateFlow(mealsRepository.categoriesState)
    val categoriesState = categories.asStateFlow()

    private var meals = MutableStateFlow(mealsRepository.mealsState)
    val mealsState = meals.asStateFlow()

    private var _chosenCategoryName = MutableStateFlow("")
    val chosenCategoryName = _chosenCategoryName.asStateFlow()

    private var _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        getAllCategories()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterMealsBySearchQuery(query)
    }

    private fun filterMealsBySearchQuery(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                getAllDishesByCategoryName(_chosenCategoryName.value)
            } else {
                val filteredMeals = mealsRepository.mealsState.result.filter {
                    it.mealName.contains(query, ignoreCase = true)
                }
                meals.value = meals.value.copy(result = filteredMeals)
            }
        }
    }

    fun setChosenCategory(name: String) {
        _chosenCategoryName.value = name
        getAllDishesByCategoryName(name)
    }

    fun getAllDishesByCategoryName(categoryName: String) {
        viewModelScope.launch {
            try {
                meals.value = meals.value.copy(isLoading = true)
                val response = mealsRepository.getAllMealsByCategoryName(categoryName)
                meals.value = meals.value.copy(
                    isLoading = false,
                    isError = false,
                    result = response.meals
                )
            } catch (e: Exception) {
                meals.value = meals.value.copy(
                    isError = true,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun getAllCategories() {
        viewModelScope.launch {
            try {
                categories.value = categories.value.copy(isLoading = true)
                val response = mealsRepository.getAllCategories()
                categories.value = categories.value.copy(
                    isLoading = false,
                    isError = false,
                    result = response.categories
                )
            } catch (e: Exception) {
                categories.value = categories.value.copy(
                    isError = true,
                    isLoading = false,
                    error = e.message
                )

            }

        }
    }
    private var _selectedMealDetails = MutableStateFlow<MealDetails?>(null)
    val selectedMealDetails = _selectedMealDetails.asStateFlow()

    fun getMealDetails(mealId: String) {
        viewModelScope.launch {
            try {
                val details = mealsRepository.getMealDetails(mealId)
                _selectedMealDetails.value = details
            } catch (_: Exception) {
            }
        }
    }
}

