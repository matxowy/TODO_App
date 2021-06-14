package com.matxowy.todoapp.ui.addedittask

import android.widget.Spinner
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matxowy.todoapp.data.Task
import com.matxowy.todoapp.data.TaskDao
import com.matxowy.todoapp.ui.ADD_TASK_RESULT_OK
import com.matxowy.todoapp.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle // zmienna do przechowania stanu aplikacji by po wyjściu z aplikacji móc wrócić do tego co było wpisywane, dzięki adnotacji @Assisted wspomagane jest wstrzykiwanie w czasie pracy
) : ViewModel() {

    val task = state.get<Task>("task") // pobieranie tasku ze stanu poprzez klucz "task", klucz odwołuje się do argumentu z AddEditTaskFragment w nav_graph.xml

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

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>() // tworzymy kanał dla addEditTaskEvent by przesyłać odpowiednie zadania do wykonania fragmentowi
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow() // zmieniamy na flow

    // funkcja potrzebna żeby uzyskać index na którym jest dana pozycja w spinnerze
    fun getIndex(spinner: Spinner, string: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(string)) {
                return i
            }
        }
        return 0
    }

    fun onSaveClick() {
        if (taskName.isBlank()) { // sprawdzamy czy taskName nie jest pusty albo nie zawiera whitespace
            showInvalidInputMessage("Nazwa nie może być pusta")
            return // return potrzebny aby nie wykonać poniższych ifów
        }

        if (task != null) { // jeżeli task nie jest pusty czyli edytujemy jakieś zadanie
            val updatedTask = task.copy(name = taskName, category = categoryName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, category = categoryName, important = taskImportance) // tworzymy nowe zadanie
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(msg: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(msg)) // przesyłamy poprzez channel informację do fragmentu o wyświetleniu komunikatu
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task) // tworzymy nowe zadanie w bazie danych
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK)) // przesyłamy rezultat o udanym dodaniu
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task) // aktualizujemy zadanie w bazie
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK)) // przesyłamy rezultat o udanym edytowaniu
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}