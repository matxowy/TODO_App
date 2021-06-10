package com.matxowy.todoapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "task_table") // ustawiamy tą klasę jako tabelę w naszej bazie danych
@Parcelize // ustawiamy ten atrybut żeby można było wysyłać ten obiekt pomiędzy różnymi fragmentami
data class Task(
    val name: String,               // wszystkie pola klasy muszą być val by przy zmianie obiektu klasy tworzyły się nowe obiekty,
    val category: String,           // ponieważ nasza aplikacja może nie rozpoznawać zmian gdy zmodyfikujemy istniejące obiekty
    val important: Boolean = false, // zmienna oznaczająca czy zadanie jest zaznaczone jako ważne
    val completed: Boolean = false, // zmienna oznaczająca czy zadanie jest zaznaczone jako ukończone
    val created: Long = System.currentTimeMillis(), // pobieramy i zapisujemy w zmiennej czas stworzenia zadania w milisekundach
    @PrimaryKey(autoGenerate = true) val id: Int = 0 // jako, że jest to nasza tabela w bazie danych potrzebny jest klucz główny, który jest tu automatycznie generowany
) : Parcelable {
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created) // ustawiamy by funkcja get zwracała sformatowaną datę

}
