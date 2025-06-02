package com.example.simpletaskapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskEntryActivity : Activity() {

    private lateinit var titleEditText: EditText
    private lateinit var descEditText: EditText
    private lateinit var categoryRadioGroup: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var isEditMode = false
    private var currentTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_entry)

        initializeViews()
        setupIntentData()
        setupListeners()
    }

    private fun initializeViews() {
        titleEditText = findViewById(R.id.titleEditText)
        descEditText = findViewById(R.id.descEditText)
        categoryRadioGroup = findViewById(R.id.categoryRadioGroup)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
    }

    private fun setupIntentData() {
        isEditMode = intent.getBooleanExtra("isEdit", false)
        currentTask = intent.getParcelableExtra("task")

        if (isEditMode && currentTask != null) {
            titleEditText.setText(currentTask?.title)
            descEditText.setText(currentTask?.description)
            when (currentTask?.category) {
                "Important" -> findViewById<RadioButton>(R.id.importantRadio).isChecked = true
                "Urgent" -> findViewById<RadioButton>(R.id.urgentRadio).isChecked = true
                else -> findViewById<RadioButton>(R.id.regularRadio).isChecked = true
            }
        } else {
            when (intent.getStringExtra("category")) {
                "Important" -> findViewById<RadioButton>(R.id.importantRadio).isChecked = true
                "Urgent" -> findViewById<RadioButton>(R.id.urgentRadio).isChecked = true
                else -> findViewById<RadioButton>(R.id.regularRadio).isChecked = true
            }
        }
    }

    private fun setupListeners() {
        saveButton.setOnClickListener { saveTask() }
        cancelButton.setOnClickListener { finish() }
    }

    private fun saveTask() {
        val title = titleEditText.text.toString().trim()
        val description = descEditText.text.toString().trim()
        val category = when (categoryRadioGroup.checkedRadioButtonId) {
            R.id.importantRadio -> "Important"
            R.id.urgentRadio -> "Urgent"
            else -> "Regular"
        }

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode && currentTask != null) {
            updateTask(currentTask!!.id, title, description, category)
        } else {
            createTask(title, description, category)
        }
    }

    private fun createTask(title: String, description: String, category: String) {
        val task = Task(
            title = title,
            description = description,
            category = category,
            status = Task.STATUS_ACTIVE, //
            updated_at = ""
        )

        ApiClient.instance.create(ApiService::class.java).addTask(task).enqueue(
            object : Callback<Task> {
                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    if (response.isSuccessful) {
                        setResult(RESULT_OK)
                        finish() // Tutup activity dan kembalikan ke TaskListActivity
                    } else {
                        Toast.makeText(
                            this@TaskEntryActivity,
                            "Failed to create task",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Task>, t: Throwable) {
                    Toast.makeText(
                        this@TaskEntryActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun updateTask(id: Int, title: String, description: String, category: String) {
        val updatedTask = Task(
            id = id,
            title = title,
            description = description,
            category = category,
            status = "active",
            updated_at = ""
        )

        ApiClient.instance.create(ApiService::class.java).updateTask(id, updatedTask).enqueue(
            object : Callback<Task> {
                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    if (response.isSuccessful) {
                        setResult(RESULT_OK)
                        finish() // Tutup activity dan kembalikan ke TaskListActivity
                    } else {
                        Toast.makeText(
                            this@TaskEntryActivity,
                            "Failed to update task",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Task>, t: Throwable) {
                    Toast.makeText(
                        this@TaskEntryActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}