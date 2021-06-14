package com.matxowy.todoapp.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.matxowy.todoapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // oznaczamy klasę by można było wstrzykiwać
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController) // funkcja zajmująca się ustawieniem działającego buttona cofania na taskbarze do głównego fragmentu
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp() // zajmuje się odpowiednim cofaniem
    }
}

// pola stworzona na potrzebę NavigateBackWithResult() by przekazywać je w tej klasie aby uniknąć możliwych problemów z użyciem tam wartości RESULT_OK
const val ADD_TASK_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_RESULT_OK = Activity.RESULT_FIRST_USER + 1 // +1 bo kolejny numer