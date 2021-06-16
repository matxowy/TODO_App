package com.matxowy.todoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

//intefejs w którym definiujemy metody potrzebne do bazy danych, nie musimy implementować podstawowych bo robi to za nas biblioteka room wystarczy dodać adnotację
@Dao
interface TaskDao {

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean) : Flow<List<Task>> =
        when(sortOrder) {
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    /*(completed != :hideCompleted OR completed = 0) - jeżeli jest coś ukończone i nie mamy ustawione żeby to ukrywać to wyszukujemy w bazie danych,
    ale jeżeli coś jest skończone i jest ustawione ukrywanie to nie wybieramy tego z bazy danych (nie spełnia warunku),
    dodatkowo by warunek był spełniony dodajemy completed = 0 co odpowiada za false bo jak mamy nieskończone zadania i ustawione na nieukrywanie
    to pierwszy warunek by nie był spełniony, a tak nie powinny być w naszym przypadku*/
    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table")
    suspend fun getAllTasks(): List<Task>

    // zapytanie służące do usuwania zakończonych zadań
    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task) //funkcja do wstawiania do bazy danych, oznaczona jako suspend bo dzięki temu może działać na innym wątku(room nie pozwala nam na operacje na głównym wątku)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}