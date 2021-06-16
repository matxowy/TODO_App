package com.matxowy.todoapp.ui.addedittask

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.matxowy.todoapp.data.TaskDatebase
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEditTaskViewModelTest : TestCase() {

    /*private lateinit var viewModel: AddEditTaskViewModel


    @Before
    override fun setUp() {
        super.setUp()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, TaskDatebase::class.java)
            .allowMainThreadQueries().build()
        val state = SavedStateHandle()

        viewModel = AddEditTaskViewModel(db.taskDao(), state)
    }

    @Test
    fun if_we_choose_create_task_that_task_create_in_database() {
        viewModel.taskName = "Przetestowac funkcje"
        viewModel.taskImportance = true

        viewModel.onSaveClick()
        val result = (viewModel.task?.name).equals("Przetestowac funkcje") && (viewModel.task?.important == true)

        assertTrue(result)
    }*/
}