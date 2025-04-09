package com.lior.automationtestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.lior.automationtestapp.ui.theme.AutomationTestAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutomationTestAppTheme {
                AppNavigation()
            }
        }
    }
}

// Navigation Routes
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object List : Screen("list")
}

// Navigation Setup
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.List.route) { ListScreen { navController.popBackStack() } }
    }
}

// ========== HOME SCREEN ==========
@Composable
fun HomeScreen(navController: NavController) {
    var textValue by remember { mutableStateOf("") }
    var sliderValue by remember { mutableStateOf(0f) }
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "App for Auto Tests",
            style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.semantics {
                contentDescription = "Open popup button"
            }
        ) {
            Text("Set Data")
        }

        Spacer(modifier = Modifier.height(8.dp))


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray) // Adds a visible border
                .padding(8.dp) // Ensures text isn't touching the border
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = {},
                enabled = false, // Keeps it disabled
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "disabled text box"
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Progress: ${sliderValue.toInt()}%",
            modifier = Modifier.semantics {
                contentDescription = "Progress bar text"
            }
        )
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "slider" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(Screen.List.route) },
            modifier = Modifier.semantics {
                contentDescription = "Open list view button"
            }
        )
        {
            Text("Create a List")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pop up text",
                modifier = Modifier.semantics {
                    contentDescription = "popup title"
                }
                ) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray) // Adds a visible border
                        .padding(8.dp) // Ensures text isn't touching the border
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "popup text box"
                            }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    textValue = inputText
                    showDialog = false
                },
                    modifier = Modifier.semantics {
                        contentDescription = "popup OK button"
                    }
                    ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false },
                    modifier = Modifier.semantics {
                    contentDescription = "popup cancel button"
                }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ========== LIST SCREEN ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(onBack: () -> Unit) {
    var items by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(
                    onClick = {
                        if (selectedItems.isNotEmpty()) {
                            items = items.filterNot { selectedItems.contains(it.first) }
                            selectedItems = emptySet()
                        }
                    },
                    modifier = Modifier.alpha(if (selectedItems.isNotEmpty()) 1f else 0.5f) // Grays out when inactive
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Item")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(items, key = { it.first }) { (item, isSelected) ->
                ListItem(
                    text = item,
                    isSelected = isSelected,
                    onToggleSelect = {
                        val newItems = items.map { (text, selected) ->
                            if (text == item) text to !selected else text to selected
                        }
                        items = newItems
                        selectedItems = newItems.filter { it.second }.map { it.first }.toSet()
                    },
                    onDelete = {
                        items = items - (item to isSelected)
                        selectedItems = selectedItems - item
                    }
                )
            }
        }
    }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add new item",
                modifier = Modifier.semantics {
                contentDescription = "popup title"
            }) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray) // Adds a visible border
                        .padding(8.dp) // Ensures text isn't touching the border
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "popup text box"
                            }
                    )
                }
            }
            ,
            confirmButton = {
                TextButton(onClick = {
                    if (inputText.text.isNotBlank()) {
                        items = items + (inputText.text to false) // New items appear at the bottom
                        inputText = TextFieldValue("")
                    }
                    showDialog = false
                },
                    modifier = Modifier.semantics {
                    contentDescription = "popup OK button"
                }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false },
                    modifier = Modifier.semantics {
                        contentDescription = "popup cancel button"
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ListItem(text: String, isSelected: Boolean, onToggleSelect: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberDismissState()

    SwipeToDismiss(
        state = dismissState,
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        },
        directions = setOf(DismissDirection.EndToStart),
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .semantics { contentDescription = if (isSelected) "Selected: $text" else "Unselected: $text" },

                onClick = onToggleSelect,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color.LightGray else Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text)
                }
            }
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == DismissValue.DismissedToStart) {
            onDelete()
        }
    }
}
