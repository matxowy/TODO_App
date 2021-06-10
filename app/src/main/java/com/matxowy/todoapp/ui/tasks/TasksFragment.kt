package com.matxowy.todoapp.ui.tasks

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.matxowy.todoapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // oznaczamy klasę by można było wstrzykiwać i viewmodel mógł się zajmować tym fragmentem
class TasksFragment: Fragment(R.layout.fragment_tasks) {

    private val viewModel: TasksViewModel by viewModels()
}