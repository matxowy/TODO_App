package com.matxowy.todoapp.ui.addedittaskfail

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.todoapp.data.Task
import com.matxowy.todoapp.data.TaskDao
import com.matxowy.todoapp.di.ApplicationScope
import com.matxowy.todoapp.ui.ADD_TASK_RESULT_OK
import com.matxowy.todoapp.ui.EDIT_TASK_RESULT_OK
import com.matxowy.todoapp.ui.addedittask.AddEditTaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.sql.SQLException

class AddEditTaskFailedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope, // potrzebujemy nowy scope, ponieważ scope dialogu kończy się gdy dialog zniknie, a dodanie elementu może zająć chwilę dłużej dlatego żeby uniknąć przerwania w połowie dajemy nowy scope na to
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("sent_task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: "" // pobieramy ze stanu nazwę zadania, jeżeli jest nullem to pobieramy z pobranego wcześniej tasku, a jak też jest nullem to pobieramy pusty string
        set(value) { // nadpisujemy metodę set by dodać wartość do stanu pod kluczem taskName
            field = value
            state.set("taskName", value)
        }

    var categoryName = state.get<String>("categoryName") ?: task?.category ?: "" // pobieramy ze stanu nazwę kategorii, jeżeli jest nullem to pobieramy z pobranego wcześniej tasku, a jak też jest nullem to pobieramy pusty string
        set(value) { // nadpisujemy metodę set by dodać wartość do stanu pod kluczem categoryName
            field = value
            state.set("categoryName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false// pobieramy ze stanu nazwę kategorii, jeżeli jest nullem to pobieramy z pobranego wcześniej tasku, a jak też jest nullem to pobieramy pusty string
    set(value) { // nadpisujemy metodę set by dodać wartość do stanu pod kluczem categoryName
        field = value
        state.set("taskImportance", value)
    }

    private val addEditTaskEventChannel = Channel<AddEditTaskViewModel.AddEditTaskEvent>() // tworzymy kanał dla addEditTaskEvent by przesyłać odpowiednie zadania do wykonania fragmentowi

    fun onRetryClick() = applicationScope.launch {
        if (task != null) { // jeżeli task nie jest pusty czyli edytujemy jakieś zadanie
            val updatedTask = task.copy(name = taskName, category = categoryName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, category = categoryName, important = taskImportance) // tworzymy nowe zadanie
            createTask(newTask)
        }
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        try {
            taskDao.insert(task) // tworzymy nowe zadanie w bazie danych
            addEditTaskEventChannel.send(AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK)) // przesyłamy rezultat o udanym dodaniu
        } catch (e: SQLException) {
            addEditTaskEventChannel.send(AddEditTaskViewModel.AddEditTaskEvent.NavigateToAddEditTaskFailedScreen(task))
        }
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        try {
            taskDao.update(task) // aktualizujemy zadanie w bazie
            addEditTaskEventChannel.send(AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK)) // przesyłamy rezultat o udanym edytowaniu
        } catch (e: SQLException) {
            addEditTaskEventChannel.send(AddEditTaskViewModel.AddEditTaskEvent.NavigateToAddEditTaskFailedScreen(task))
        }
    }

}