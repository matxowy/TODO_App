package com.matxowy.todoapp.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.matxowy.todoapp.R
import com.matxowy.todoapp.data.SortOrder
import com.matxowy.todoapp.data.Task
import com.matxowy.todoapp.databinding.FragmentTasksBinding
import com.matxowy.todoapp.util.exhaustive
import com.matxowy.todoapp.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint // oznaczamy klasę by można było wstrzykiwać i viewmodel mógł się zajmować tym fragmentem
class TasksFragment: Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

    private val viewModel: TasksViewModel by viewModels()

    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view) // FragmentTasksBinding jest automatycznie generowany w tym przypadku z layoutu fragment_tasks

        val taskAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter // ustawiamy adapter recyclerView na nasz
                layoutManager = LinearLayoutManager(requireContext()) // ustawiamy layoutManager na liniowy layout
                setHasFixedSize(true) // w celu optymalizacji
            }

            viewModel.onNoTasksToShow() // jeżeli nie ma wpisanych jeszcze żadnych zadań przy starcie aplikacji włącza widoczność komunikatu o dodaniu pierwszego zadania

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, // 0 bo nie chcemy obsługiwać drag and drop akcji
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false // nie zajmujemy się onMove bo on jest odpowiedzialny za drag and drop
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition] // pobieramy do zmiennej to zadanie które zostało swipenięte
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks) // załączamy możliwość swipeowania do recyclerView

            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle -> // ustawiamy nasłuchiwanie na FragmentResult pod kluczem "add_edit_request" i gdy taki dostaniemy wykonujemy poniższy kod
            val result = bundle.getInt("add_edit_result") // zapisujemy rezultat z przekazanego bundle
            viewModel.onAddEditResult(result) // odelegowujemy do viewModelu wykonanie czynności z rezultatem
        }

        viewModel.tasks.observe(viewLifecycleOwner) { // obserwujemy zmiany w bazie danych i jak zostanie dokonana jakaś zmiana
            taskAdapter.submitList(it)                // wyowołujemy submitList, która zajmie się kalkulowaniem zmian ze starej listy
            viewModel.onNoTasksToShow()               // jeżeli usuniemy wszystkie elementy z bazy danych to chcemy pokazać komunikat o dodaniu pierwszego zadania
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted { // launchWhenStared pozwoli odpalać tylko snackbara gdy tasksFragment będzie na przodzie
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {                     // jeżeli fragment dostaje od TasksViewModelu żeby pokazać snackbar to
                        Snackbar.make(requireView(), "Zadanie usunięte", Snackbar.LENGTH_LONG) // tworzy snackbar z napisem zadanie usunięte
                            .setAction("COFNIJ") {                                             // ustawiana jest akcja pod napisem "COFNIJ" na snackbarze
                                viewModel.onUndoDeleteClick(event.task)                             // wywołujemy funckję w viewModel, która przywróci nam zadanie przekazane w parametrze
                            }.show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(null, "Nowe zadanie") // actionTasksFragmentToAddEditTaskFragment funkcja wygenerowana automatyczne po zaznaczeniu nawigacji w nav_graph
                        findNavController().navigate(action) // kod odpowiadający za przenoszenie nas do odpowiedniego fragmentu
                    }
                    is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task, "Edycja zadania") // w fragmencie do edytowania w parametrze przekazujemy zadanie edytowane
                        findNavController().navigate(action) // kod odpowiadający za przenoszenie nas do odpowiedniego fragmentu
                    }
                    is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment() // definiujemy akcję pokazania dialogu
                        findNavController().navigate(action) // przechodzimy do zdefiniowanego fragmentu
                    }
                    is TasksViewModel.TasksEvent.ShowMessageAboutAddFirstTask -> {
                        message_add_task.isVisible = event.shouldShow // ustawiamy widoczność w zależności od tego co przekazał viewModel
                    }
                }.exhaustive // zamienia when w wyrażenie
            }
        }

        setHasOptionsMenu(true) // potrzebne by menu się wyświetlało
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu) // wstawiamy nasze stworzone menu w menu przekazane w parametrze czyli do naszego fragmentu

        // przygotowania do tego by można było wyszukiwać zadania
        val searchIteam = menu.findItem(R.id.action_search)
        searchView = searchIteam.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value // zapisujemy w zmiennej aktualnie wyszukiwaną frazę
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchIteam.expandActionView()
            searchView.setQuery(pendingQuery, false) // wrzucamy do wyszukiwarki naszą frazę
        }

        searchView.onQueryTextChanged { // w it który jest stringiem dostajemy to co jest wpisywane w polu szukania
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted // odczytujemy z preferencesFlow wartość hideCompleted i ustawiamy nasz checkBox na odpowiedni
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) { // w when sprawdzamy które pole zostało kliknięte i wtedy wykonujemy jakieś czynności, return przed when oznacza, że ostatnie wyrażenie jest zwracane
            R.id.action_sory_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME) // ustawiamy sortowanie po nazwie
                true
            }
            R.id.action_sory_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE) // ustawiamy sortowanie po dacie
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked // podczas kliknięcia przypisujemy odwrotną wartość czyli jeżeli był zaznaczony to odznaczamy
                viewModel.onHideCompletedClick(item.isChecked) // ustawiamy wartość z pola item isChecked
                true
            }
            R.id.action_delete_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null) // usuwamy listener dzięki czemu przy niszczeniu nie wysyła nam niepotrzebnego pustego stringa do zmiennej
    }
}