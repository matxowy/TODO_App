package com.matxowy.todoapp.di

import android.app.Application
import androidx.room.Room
import com.matxowy.todoapp.data.TaskDatebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

// w tym obiekcie wykonujemy wstrzykiwanie zależności
@Module
@InstallIn(ApplicationComponent::class) // ustawiamy by w całej aplikacji była dostępna instancja bazy danych
object AppModule {

    @Provides
    @Singleton // oznaczamy by utworzyła się jedna instancja
    fun provideDatabase( // funkcja zwraca bazę danych
        app: Application,
        callback: TaskDatebase.Callback
    ) = Room.databaseBuilder(app, TaskDatebase::class.java, "task_database") // tworzymy bazę danych przez bilbiotekę Room
            .fallbackToDestructiveMigration() // strategia migracji ustawiona została by usuwała tablicę i tworzyła nową
            .addCallback(callback) // dodajemy jakieś wartości do bazy początkowe
            .build() // tworzymy jedną instację naszej bazy danych

    @Provides
    fun provideTaskDao(db: TaskDatebase) = db.taskDao()

    @ApplicationScope
    @Provides
    @Singleton // nasz scope będzie żyć tak długo jak aplikacja będzie żyć
    fun provideApplicationScope() = CoroutineScope(SupervisorJob()) // tworzymy nasz własny scope dla aplikacji, SupervisorJob ustawia by nasza aplikacja mimo błędu jakiegoś dziecka nie zatrzymywała innych
}

// by w przyszłości rozróżniać scope musimy to dodać
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope