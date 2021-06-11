package com.matxowy.todoapp.util

import androidx.appcompat.widget.SearchView

// tworzymy dodatkową rozszerzającą funkcję w SearchView
inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {

    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true // zwracamy true bo nie musimy się tym zajmować, ponieważ nie mamy przycisku do wyszukiwania
        }

        override fun onQueryTextChange(newText: String?): Boolean { // wyszukujemy przy każdej zmianie tekstu dlatego implementujemy ciało tej metody
            listener(newText.orEmpty()) // zwraca pustego stringa jeżeli newText nie jest znany
            return true
        }
    })
}