package com.matxowy.todoapp.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.matxowy.todoapp.data.PreferencesRepository
import com.matxowy.todoapp.data.SortOrder
import com.matxowy.todoapp.data.Task
import com.matxowy.todoapp.data.TaskDao
import com.matxowy.todoapp.ui.ADD_TASK_RESULT_OK
import com.matxowy.todoapp.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesRepository: PreferencesRepository, // zostanie automatycznie wstrzyknięty bo dostał adnotację @Incject w klasie
    @Assisted private val state: SavedStateHandle // zmienna do przechowania stanu aplikacji by po wyjściu z aplikacji móc wrócić do tego co było wpisywane w pasku wyszukiwania
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesRepository.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow = combine( // wszystkie mutable values przekazujemy tym do jednego flow
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences) // musimy zwrócić wszystkie wartości dlatego opakowujemy to funkcją Pair
    }.flatMapLatest { (query, filterPreferences) -> // używamy lamby by uniknąć pisania w getTasks it.query itd. flatmap obserwuje zmienianie wartości na bieżąco
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted) // jeżeli searchQuery, sortOrder lub hideCompleted się zmienia to wykonuje się ten kod z parametrami przekazanymi
    }

    val tasks = tasksFlow.asLiveData() // z naszego interfejsu pobieramy metodą getTask() listę tasków

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch { // updateSortOrder jest suspend fun dlatego musimy to odpalić w viewModelScope
        preferencesRepository.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch { // updateHideCompleted jest suspend fun dlatego musimy to odpalić w viewModelScope
        preferencesRepository.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked)) // musimy użyć funkcji copy bo pola w copy są val więc nie możemy dokonywać na nich zmian
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task) // gdy klikniemy na cofnięcie musimy dodać znowu do bazy poprzez insert nasze usunięte zadanie
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Zadanie dodane")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Zadanie zaktualizowane")
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch { // tylko fragment może pokazać snackbar dlatego robimy to w viewModelScope
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent() // tworzymy typ object bo nie musimy tu nic przekazywać
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent() // klasa, ponieważ przekazujemy parametr
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
    }


}
