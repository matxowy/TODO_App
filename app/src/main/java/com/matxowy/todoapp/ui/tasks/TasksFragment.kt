package com.matxowy.todoapp.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.matxowy.todoapp.R
import com.matxowy.todoapp.databinding.FragmentTasksBinding
import com.matxowy.todoapp.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // oznaczamy klasę by można było wstrzykiwać i viewmodel mógł się zajmować tym fragmentem
class TasksFragment: Fragment(R.layout.fragment_tasks) {

    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view) // FragmentTasksBinding jest automatycznie generowany w tym przypadku z layoutu fragment_tasks

        val taskAdapter = TasksAdapter()

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter // ustawiamy adapter recyclerView na nasz
                layoutManager = LinearLayoutManager(requireContext()) // ustawiamy layoutManager na liniowy layout
                setHasFixedSize(true) // w celu optymalizacji
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner) { // obserwujemy zmiany w bazie danych i jak zostanie dokonana jakaś zmiana
            taskAdapter.submitList(it)                // wyowołujemy submitList, która zajmie się kalkulowaniem zmian ze starej listy
        }

        setHasOptionsMenu(true) // potrzebne by menu się wyświetlało
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu) // wstawiamy nasze stworzone menu w menu przekazane w parametrze czyli do naszego fragmentu

        // przygotowania do tego by można było wyszukiwać zadania
        val searchIteam = menu.findItem(R.id.action_search)
        val searchView = searchIteam.actionView as SearchView

        searchView.onQueryTextChanged { // w it który jest stringiem dostajemy to co jest wpisywane w polu szukania
            viewModel.searchQuery.value = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) { // w when sprawdzamy które pole zostało kliknięte i wtedy wykonujemy jakieś czynności, return przed when oznacza, że ostatnie wyrażenie jest zwracane
            R.id.action_sory_by_name -> {
                viewModel.sortOrder.value = SortOrder.BY_NAME // ustawiamy sortowanie po nazwie
                true
            }
            R.id.action_sory_by_date_created -> {
                viewModel.sortOrder.value = SortOrder.BY_DATE // ustawiamy sortowanie po dacie
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked // podczas kliknięcia przypisujemy odwrotną wartość czyli jeżeli był zaznaczony to odznaczamy
                viewModel.hideCompleted.value = item.isChecked // ustawiamy wartość z pola item isChecked
                true
            }
            R.id.action_delete_completed_tasks -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}