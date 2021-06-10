package com.matxowy.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matxowy.todoapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatebase : RoomDatabase() { // klasa jest abstrakcyjna bo bilbioteka room zajmuje się implementacją

    abstract fun taskDao(): TaskDao // abstract, ponieważ room pobiera ciało tej funkcji z klasy TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatebase>, // jesteśmy w środku klasy bazy danych więc by ją zapewnić musimy ją przekazać w konstruktorze przez provider żeby nie zamknąć się w circular injection
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val taskDao = database.get().taskDao() //pobieramy z naszej provide database metodę taskDao i dajemy do funkcji by się nią posługiwać

            applicationScope.launch {
                taskDao.insert(Task("Napisać projekt", "Praca", important = true))
                taskDao.insert(Task("Obrać ziemniaki", "Inne"))
                taskDao.insert(Task("Kupić mięso", "Zakupy"))
                taskDao.insert(Task("Przygotować obiad", "Inne", completed = true))
                taskDao.insert(Task("Umyć naczynia", "Inne", completed = true))
                taskDao.insert(Task("Zadzwonić do prezesa", "Praca"))
            }
        }
    }
}