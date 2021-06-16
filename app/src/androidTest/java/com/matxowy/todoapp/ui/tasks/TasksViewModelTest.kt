package com.matxowy.todoapp.ui.tasks

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.matxowy.todoapp.data.PreferencesRepository
import com.matxowy.todoapp.data.TaskDatebase
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksViewModelTest : TestCase() {

    /*private lateinit var viewModel: TasksViewModel

    @Before
    override fun setUp() {
        super.setUp()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, TaskDatebase::class.java)
            .allowMainThreadQueries().build()
        val preferences = PreferencesRepository(context)
        val state = SavedStateHandle()

        viewModel = TasksViewModel(db.taskDao(), preferences, state)
    }

    @Test
    fun hiding_completed_tasks_after_click_on_hide_completed_task() {

    }*/
}