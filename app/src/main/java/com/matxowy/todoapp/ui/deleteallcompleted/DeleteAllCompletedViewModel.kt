package com.matxowy.todoapp.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.matxowy.todoapp.data.TaskDao
import com.matxowy.todoapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope // potrzebujemy nowy scope, ponieważ scope dialogu kończy się gdy dialog zniknie, a usuwanie elementów może zająć chwilę dłużej dlatego żeby uniknąć przerwania w połowie dajemy nowy scope na to
) : ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks() // po potwierdzeniu w dialogu usunięcia wykonujemy usunięcie z bazy danych poprzez metodę w taskDao
    }
}