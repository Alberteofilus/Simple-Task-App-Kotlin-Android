package com.example.simpletaskapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskAdapter(
    private val context: Context,
    private val tasks: MutableList<Task>
) : BaseAdapter() {

    interface TaskActionListener {
        fun onTaskUpdated()
        fun onTaskUpdateFailed(message: String)
    }

    private var listener: TaskActionListener? = null

    fun setTaskActionListener(listener: TaskActionListener) {
        this.listener = listener
    }

    override fun getCount(): Int = tasks.size
    override fun getItem(position: Int): Task = tasks[position]
    override fun getItemId(position: Int): Long = tasks[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = tasks[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false)

        view.apply {
            findViewById<TextView>(R.id.taskTitle).text = task.title
            findViewById<TextView>(R.id.taskCategory).text = "Category: ${task.category}"
            findViewById<TextView>(R.id.taskUpdatedAt).text = "Updated: ${task.updated_at}"
            findViewById<TextView>(R.id.taskDescription).text = task.description

            val editBtn = findViewById<Button>(R.id.editButton)
            val doneBtn = findViewById<Button>(R.id.doneButton)
            val deleteBtn = findViewById<Button>(R.id.deleteButton)

            val isTaskDone = task.status.equals("done", ignoreCase = true)

            editBtn.isEnabled = !isTaskDone
            deleteBtn.isEnabled = !isTaskDone
            doneBtn.isEnabled = !isTaskDone

            if (isTaskDone) {
                setBackgroundColor(ContextCompat.getColor(context, R.color.task_done))
                doneBtn.text = "Done"
            } else {
                setBackgroundColor(ContextCompat.getColor(context, R.color.task_active))
                doneBtn.text = "Mark Done"
            }

            editBtn.setOnClickListener { editTask(task) }
            doneBtn.setOnClickListener { markTaskDone(task, position) }
            deleteBtn.setOnClickListener { deleteTask(task, position) }
        }

        return view
    }

    private fun editTask(task: Task) {
        if (task.status == "done") {
            Toast.makeText(context, "Completed tasks cannot be edited", Toast.LENGTH_SHORT).show()
            return
        }

        context.startActivity(Intent(context, TaskEntryActivity::class.java).apply {
            putExtra("task", task)
            putExtra("isEdit", true)
        })
    }

    private fun markTaskDone(task: Task, position: Int) {
        // Optimistic UI update
        tasks[position] = task.copy(status = "done")
        notifyDataSetChanged()

        ApiClient.instance.create(ApiService::class.java)
            .updateStatus(task.id, mapOf("status" to "done"))
            .enqueue(object : Callback<Task> {
                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    if (!response.isSuccessful) {
                        // Revert if failed
                        tasks[position] = task
                        notifyDataSetChanged()
                        listener?.onTaskUpdateFailed("Failed to mark task as done")
                    } else {
                        listener?.onTaskUpdated()
                    }
                }

                override fun onFailure(call: Call<Task>, t: Throwable) {
                    tasks[position] = task
                    notifyDataSetChanged()
                    listener?.onTaskUpdateFailed("Error: ${t.message}")
                }
            })
    }

    private fun deleteTask(task: Task, position: Int) {
        if (task.status == "done") {
            Toast.makeText(context, "Completed tasks cannot be deleted", Toast.LENGTH_SHORT).show()
            return
        }

        // Optimistic removal
        val removedTask = tasks.removeAt(position)
        notifyDataSetChanged()

        ApiClient.instance.create(ApiService::class.java)
            .deleteTask(task.id)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        // Revert if failed
                        tasks.add(position, removedTask)
                        notifyDataSetChanged()
                        listener?.onTaskUpdateFailed("Failed to delete task")
                    } else {
                        listener?.onTaskUpdated() //
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    tasks.add(position, removedTask)
                    notifyDataSetChanged()
                    listener?.onTaskUpdateFailed("Error: ${t.message}")
                }
            })
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}