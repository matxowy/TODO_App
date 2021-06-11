package com.matxowy.todoapp.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matxowy.todoapp.data.Task
import com.matxowy.todoapp.databinding.ItemTaskBinding

// rozszerzamy naszą klasę o ListAdapter, ponieważ ListAdapter radzi sobie z całymi listami, a takie właśnie będziemy dostawać
class TasksAdapter : ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    // gdy recylerView potrzebuje nowego obiektu do listy wykorzystuje tą metodę
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position) // pobieramy odpowiedni item
        holder.bind(currentItem) // bindujemy przez holder ten item
    }

    class TasksViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) { // w ViewHolder przekazujemy przez binding.root layout item_task

        fun bind(task: Task) { // tą funkcją będziemy przekazywać poszczególne dane do naszego layoutu
            binding.apply { // używamy funkcji apply by zapobiec powtarzaniu zmiennej binding
                textViewItemName.text = task.name // binding zamienia nazwy zmiennych ze SnakeCase na CamelCase
                textViewItemName.paint.isStrikeThruText = task.completed // jeżeli zadanie jest wykonane to skreślamy nazwę
                checkBoxCompleted.isChecked = task.completed // checkBox jest zaznaczony jeżeli zadanie jest oznaczone jako wykonane
                imageViewPriority.isVisible = task.important // jeżeli zadanie jest ważne wyświetlamy wykrzyknik
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>(){
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id // poprzez id porównujemy czy itemy są takie same

        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem // dataClass zapewnia nam porównanie dwóch obiektów daty przez == dlatego możemy w łatwy sposób zwrócić true lub false przy porównaniu tą metodą
    }
}