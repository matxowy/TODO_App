package com.matxowy.todoapp.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.matxowy.todoapp.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    // określamy startowe wartości sortowania i ukrywania
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    val hideCompleted = MutableStateFlow(false)

    private val tasksFlow = combine( // wszystkie mutable values przekazujemy tym do jednego flow
        searchQuery,
        sortOrder,
        hideCompleted
    ) { query, soryOrder, hideCompleted ->
        Triple(query, soryOrder, hideCompleted) // musimy zwrócić wszystkie wartości dlatego opakowujemy to funkcją Triple
    }.flatMapLatest { (query, sortOrder, hideCompleted) -> // używamy lamby by uniknąć pisania w getTasks it.query itd.
        taskDao.getTasks(query, sortOrder, hideCompleted) // jeżeli searchQuery, sortOrder lub hideCompleted się zmienia to wykonuje się ten kod gdzie z parametrami przekazanymi
    }

    val tasks = tasksFlow.asLiveData() // z naszego interfejsu pobieramy metodą getTask() listę tasków
}

enum class SortOrder { BY_NAME, BY_DATE } // tworzymy klasę by przez nią określać stany sortowania