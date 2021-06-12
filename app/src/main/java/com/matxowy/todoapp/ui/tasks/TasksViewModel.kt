package com.matxowy.todoapp.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.matxowy.todoapp.data.PreferencesRepository
import com.matxowy.todoapp.data.SortOrder
import com.matxowy.todoapp.data.Task
import com.matxowy.todoapp.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesRepository: PreferencesRepository // zostanie automatycznie wstrzyknięty bo dostał adnotację @Incject w klasie
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val preferencesFlow = preferencesRepository.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow = combine( // wszystkie mutable values przekazujemy tym do jednego flow
        searchQuery,
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

    fun onTaskSelected(task: Task) {
        TODO("Not yet implemented")
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

    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
    }


}
