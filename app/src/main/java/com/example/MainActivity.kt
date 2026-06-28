package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.DocumentDatabase
import com.example.data.DocumentRepository
import com.example.ui.AppNavigation
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Database & Repository
    val database = DocumentDatabase.getDatabase(applicationContext)
    val repository = DocumentRepository(database.documentDao())
    
    // Instantiate ViewModel with Custom Factory
    val viewModel = ViewModelProvider(
      this, 
      AppViewModelFactory(application, repository)
    )[AppViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          AppNavigation(viewModel = viewModel)
        }
      }
    }
  }
}
