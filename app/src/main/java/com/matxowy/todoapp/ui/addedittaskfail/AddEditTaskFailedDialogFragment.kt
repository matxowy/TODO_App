package com.matxowy.todoapp.ui.addedittaskfail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditTaskFailedDialogFragment : DialogFragment() {

    private val viewModel: AddEditTaskFailedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Błąd")
            .setMessage("Błąd podczas dodawania zadania do bazy danych")
            .setNegativeButton("Anuluj", null)
            .setPositiveButton("Ponów") { _, _ ->
                viewModel.onRetryClick()
            }
            .create()
}