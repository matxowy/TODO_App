package com.matxowy.todoapp.ui.addedittask

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.matxowy.todoapp.R
import com.matxowy.todoapp.databinding.FragmentAddEditTaskBinding
import com.matxowy.todoapp.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: AddEditTaskViewModel by viewModels()
    lateinit var array: Array<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        array = resources.getStringArray(R.array.categories_of_task) // zainiciować tabelę z resources można dopiero po załączeniu fragmentu do aktywności dlatego robimy to w onAttach
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextTaskName.setText(viewModel.taskName) // ustawiamy pole w inpucie na takie jakie uzyskamy z viewModel ze zmiennej taskName
            spinnerCategories.setSelection(viewModel.getIndex(spinnerCategories, viewModel.categoryName)) // ustawiamy pole w inpucie na takie jakie uzyskamy z viewModel ze zmiennej categoryName
            checkBoxImportantTask.isChecked = viewModel.taskImportance // ustawiamy odpowiednio checkBoxa
            checkBoxImportantTask.jumpDrawablesToCurrentState() // zaznaczanie checkBoxa ma defaultowo animacje i tą metodą ją omijamy żeby nie wyglądało to źle, że jak wejdziemy w task to go zaznacza
            textViewDateCreated.isVisible = viewModel.task != null // data utworzenia widoczna tylko gdy task nie jest pusty
            textViewDateCreated.text = "Utworzono: ${viewModel.task?.createdDateFormatted}" // ustawiamy  datę utworzenia zadania

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {} // jeżeli ktoś nie wybierze nic to nic nie robimy co sprawi, że zostanie wartość która jest już w spinnerze

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.categoryName = array[position] // jeżeli ktoś wybrał pozycję z listy przypisujemy ją do categoryName używając tablicy na wybranej pozycji
                }
            }

            checkBoxImportantTask.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus() // zamykamy klawiaturę po cofnięciu
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result) // w bundle wrzucamy EDIT_TASK_RESULT_OK lub ADD_TASK_RESULT_OK w parze z kluczem
                        )
                        findNavController().popBackStack() // usuwa ten fragment z backStacku i wraca do poprzedniego
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show() // pokazujemy snackbara z informacją o braku nazwy
                    }
                }.exhaustive
            }
        }
    }
}