package com.matxowy.todoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

//intefejs w którym definiujemy metody potrzebne do bazy danych, nie musimy implementować podstawowych bo robi to za nas biblioteka room wystarczy dodać adnotację
@Dao
interface TaskDao {
    @Query("SELECT * FROM task_table")
    fun getTask(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task) //funkcja do wstawiania do bazy danych, oznaczona jako suspend bo dzięki temu może działać na innym wątku(room nie pozwala nam na operacje na głównym wątku)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}