package com.example.simpletaskapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : Activity() {

    private lateinit var btnAll: Button
    private lateinit var btnImportant: Button
    private lateinit var btnUrgent: Button
    private lateinit var btnRegular: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchTaskCounts()
    }

    private fun initializeViews() {
        btnAll = findViewById(R.id.btnAllTasks)
        btnImportant = findViewById(R.id.btnImportant)
        btnUrgent = findViewById(R.id.btnUrgent)
        btnRegular = findViewById(R.id.btnRegular)
    }

    private fun setClickListeners() {
        btnAll.setOnClickListener { openTaskList("All") }
        btnImportant.setOnClickListener { openTaskList("Important") }
        btnUrgent.setOnClickListener { openTaskList("Urgent") }
        btnRegular.setOnClickListener { openTaskList("Regular") }
    }

    private fun openTaskList(category: String) {
        Intent(this, TaskListActivity::class.java).apply {
            putExtra("category", category)
            startActivity(this)
        }
    }

    private fun fetchTaskCounts() {
        ApiClient.instance.create(ApiService::class.java).getAllTasks().enqueue(
            object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.counts?.let { counts ->
                            updateButtonCounts(counts)
                        }
                    } else {
                        showToast("Failed to load task counts")
                    }
                }

                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                }
            }
        )
    }

    private fun updateButtonCounts(counts: Map<String, Int>) {
        btnAll.text = "All Tasks (${counts["all"] ?: 0})"
        btnImportant.text = "Important (${counts["important"] ?: 0})"
        btnUrgent.text = "Urgent (${counts["urgent"] ?: 0})"
        btnRegular.text = "Regular (${counts["regular"] ?: 0})"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}