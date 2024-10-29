package com.example.mealjsonexample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil3.compose.AsyncImage
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun MyText() {
    Text(
        text = "Hello, Jetpack Compose!",
        style = TextStyle(
            fontSize = 20.sp,
            color = Color(0xFFADD8E6)
        )
    )
}

@Composable
fun Navigation(
    modifier: Modifier,
    navigationController: NavHostController,
) {
    val viewModel: MealsViewModel = viewModel()
    NavHost(
        modifier = modifier,
        navController = navigationController,
        startDestination = Graph.mainScreen.route
    ) {
        composable(route = Graph.mainScreen.route) {
            MainScreen(viewModel, navigationController)
        }
        composable(route = Graph.secondScreen.route) {
            SecondScreen(viewModel, navigationController)
        }
        composable(route = "${Graph.mealDetailsScreen.route}/{mealId}") { backStackEntry ->
            val mealId = backStackEntry.arguments?.getString("mealId")
            mealId?.let { viewModel.getMealDetails(it) }
            MealDetailsScreen(viewModel) {
                navigationController.popBackStack() 
            }
        }
    }
}

@Composable
fun SecondScreen(viewModel: MealsViewModel, navigationController: NavHostController) {
    val categoryName = viewModel.chosenCategoryName.collectAsState()
    val dishesState = viewModel.mealsState.collectAsState()
    val searchQuery = viewModel.searchQuery.collectAsState()

    Column {
        TextField(
            value = searchQuery.value,
            onValueChange = { newValue ->
                viewModel.updateSearchQuery(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Введите блюдо") },
            maxLines = 1,
            singleLine = true,
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.updateSearchQuery("")
                    }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        when {
            dishesState.value.isLoading -> {
                LoadingScreen()
            }
            dishesState.value.isError -> {
                ErrorScreen(dishesState.value.error!!)
            }
            dishesState.value.result.isNotEmpty() -> {
                val filteredDishes = dishesState.value.result.filter {
                    it.mealName.contains(searchQuery.value, ignoreCase = true)
                }
                DishesScreen(filteredDishes, navigationController)
            }
            else -> {
                Text(
                    text = "Блюда не найдено",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}


@Composable
fun DishesScreen(result: List<Meal>, navigationController: NavHostController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(result) { meal ->
            DishItem(meal) { mealId ->
                navigationController.navigate("${Graph.mealDetailsScreen.route}/$mealId")
            }
        }
    }
}


@Composable
fun DishItem(meal: Meal, onItemClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(color = Color(0xFFFFA500))
            .clickable { onItemClick(meal.idMeal) }
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(modifier = Modifier.height(80.dp), model = meal.strMealThumb, contentDescription = null)
            Spacer(Modifier.height(5.dp))
            Text(
                text = meal.mealName,
                color = Color.White,
            )
        }
    }
}



@Composable
fun MainScreen(viewModel: MealsViewModel, navigationController: NavHostController) {

    val categoriesState = viewModel.categoriesState.collectAsState()

    if (categoriesState.value.isLoading) {
        LoadingScreen()
    }
    if (categoriesState.value.isError) {
        ErrorScreen(categoriesState.value.error!!)
    }
    if (categoriesState.value.result.isNotEmpty()) {
        CategoriesScreen(viewModel, categoriesState.value.result, navigationController)
    }
}

@Composable
fun CategoriesScreen(viewModel: MealsViewModel, result: List<Category>, navigationController: NavHostController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        items(result) {
            CategoryItem(viewModel, it, navigationController)
        }
    }
}

@Composable
fun CategoryItem(viewModel: MealsViewModel, category: Category, navigationController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(color = Color.White)
            .clickable {
                viewModel.setChosenCategory(category.strCategory)
                navigationController.navigate("${Graph.secondScreen.route}")
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = category.strCategoryThumb,
                contentDescription = null,
                modifier = Modifier.height(100.dp)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = category.strCategory
            )
        }
    }
}

@Composable
fun ErrorScreen(error: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error
        )
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun MealDetailsScreen(viewModel: MealsViewModel, onBackClick: () -> Unit) {
    val mealDetails by viewModel.selectedMealDetails.collectAsState()

    mealDetails?.let { details ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(6.dp)
                .background(color = Color(0xFFFFA500))
        ) {
            AsyncImage(
                model = details.strMealThumb,
                contentDescription = details.strMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )

            Text(
                text = details.strMeal,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 10.dp)
            )

                Text(
                    text = "details.strInstructions",
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = Color(0xFFADD8E6)
                    )
                )


            Text(
                text = "Category: ${details.strCategory}",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Area: ${details.strArea}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Instructions:",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 15.dp),
                textAlign = TextAlign.Center

            )

            Text(
                text = details.strInstructions,
                style = MaterialTheme.typography.bodyLarge

            )

            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 21.dp)
            ) {
                Text("return")
            }
        }
    } ?: run {
    }
}

