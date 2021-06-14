package com.matxowy.todoapp.ui.deleteallcompleted

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedDialogFragment : DialogFragment() {

    private val viewModel: DeleteAllCompletedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Potwierdzenie usunięcia")
            .setMessage("Na pewno chcesz usunąć wszystkie skończone zadania?")
            .setNegativeButton("Anuluj", null) // przekazujemy dla listenera null co oznacza, że nie chcemy wykonywać dodatkowych zadań przy anulowaniu tylko wyłączyć dialog
            .setPositiveButton("Tak") { _, _ ->
                viewModel.onConfirmClick() // w przypadku kliknięcia tak viewModel zajmuje się tym co się stanie
            }
            .create()
}