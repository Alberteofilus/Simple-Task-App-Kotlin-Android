package com.example.simpletaskapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskListActivity : Activity(), TaskAdapter.TaskActionListener {

    private lateinit var categoryTitle: TextView
    private lateinit var taskCountText: TextView
    private lateinit var taskListView: ListView
    private lateinit var addTaskButton: Button
    private lateinit var backButton: Button
    private lateinit var taskAdapter: TaskAdapter
    private var currentCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        initializeViews()
        setupListeners()
        loadTasks()
    }

    private fun initializeViews() {
        categoryTitle = findViewById(R.id.categoryTitle)
        taskCountText = findViewById(R.id.taskCountText)
        taskListView = findViewById(R.id.taskListView)
        addTaskButton = findViewById(R.id.addTaskButton)
        backButton = findViewById(R.id.backButton)

        currentCategory = intent.getStringExtra("category") ?: "All"
        categoryTitle.text = "Category: $currentCategory"

        taskAdapter = TaskAdapter(this, mutableListOf())
        taskAdapter.setTaskActionListener(this)
        taskListView.adapter = taskAdapter
    }

    private fun setupListeners() {
        addTaskButton.setOnClickListener {
            startActivityForResult(
                Intent(this, TaskEntryActivity::class.java).apply {
                    putExtra("category", currentCategory)
                },
                REQUEST_ADD_TASK
            )
        }

        backButton.setOnClickListener { finish() }

        taskListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val task = taskAdapter.getItem(position)
            if (task.status != "done") {
                startActivityForResult(
                    Intent(this, TaskEntryActivity::class.java).apply {
                        putExtra("task", task)
                        putExtra("isEdit", true)
                    },
                    REQUEST_EDIT_TASK
                )
            } else {
                Toast.makeText(this, "Completed tasks cannot be edited", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadTasks() {
        val apiService = ApiClient.instance.create(ApiService::class.java)
        val call = if (currentCategory == "All") {
            apiService.getAllTasks()
        } else {
            apiService.getTasksByCategory(currentCategory)
        }

        call.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { taskResponse ->
                        val tasks = taskResponse.tasks
                        taskAdapter.updateTasks(tasks.sortedByDescending { it.updated_at })
                        taskCountText.text = "Tasks: ${tasks.size}"
                    }
                } else {
                    Toast.makeText(this@TaskListActivity, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Toast.makeText(this@TaskListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Di TaskListActivity.kt
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_TASK || requestCode == REQUEST_EDIT_TASK) {
            if (resultCode == RESULT_OK) {
                loadTasks()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    override fun onTaskUpdated() {
        // Memuat ulang data dan count
        loadTasks()

        // Atau jika ingin lebih efisien tanpa memuat ulang semua data:
        // updateTaskCount()
    }

    // Tambahkan method baru untuk update count saja
    private fun updateTaskCount() {
        ApiClient.instance.create(ApiService::class.java).getAllTasks().enqueue(
            object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { taskResponse ->
                            taskCountText.text = "Tasks: ${taskAdapter.count}"
                        }
                    }
                }

                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(this@TaskListActivity, "Failed to update count", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onTaskUpdateFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_ADD_TASK = 1
        private const val REQUEST_EDIT_TASK = 2
    }
}