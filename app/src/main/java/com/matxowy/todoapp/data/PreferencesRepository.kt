package com.matxowy.todoapp.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesRepository"

enum class SortOrder { BY_NAME, BY_DATE } // tworzymy klasę by przez nią określać stany sortowania

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean) // klasa do enkapsulacji wartości sortowania i ukrywania

@Singleton // ograniczamy tworzenie obiektów tej klasy do jednej instancji i zapewniamy globalny dostęp do stworzonego obiektu
class PreferencesRepository @Inject constructor(@ApplicationContext context: Context) { // klasa należy do całej aplikacji więc ma odnośnik, że kontekst pobieramy z aplikacji

    private val dataStore = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data // chcemy by zmienna była dostępna globalnie dlatego nie używamy private
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences: ", exception) // wyrzucamy w logi komunikat w razie wystąpienia wyjątku
                emit(emptyPreferences()) // w razie wyjątku do mapowania przekazujemy null co przełoży się na wyświetlenie w podstawowych ustawieniach, ale appka nie scrashuje
            } else {
                throw exception // w przypadku innego wyjątku wyrzucamy go by móc nim się zająć
            }
        }
        .map { preferences -> // by łatwiej było poszczególnymi danymi zarządać z tego flow co dostajemy transformujemy je
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name // pobieramy z preferences jakie jest sortowanie, w razie gdy jest null ustawiamy standardowe
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false // w wypadku nulla ustawiamy na false

            FilterPreferences(sortOrder, hideCompleted) // przekazujemy wartości do klasy
        }

    // funkcja służąca zmianie wartości preferencji sortowania w dataStore
    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    // funkcja służąca zmianie wartości preferencji ukrywania w dataStore
    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    private object PreferencesKeys { // obudowujemy to w obiekt by odwołania w kodzie były bardziej czytelne
        val SORT_ORDER = preferencesKey<String>("sort_order") // tworzymy klucz preferencji dla sortowania
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed") // tworzymy klucz preferencji dla ukrywania
    }
}