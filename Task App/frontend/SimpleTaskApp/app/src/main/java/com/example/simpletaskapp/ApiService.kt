package com.example.simpletaskapp

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("tasks")
    fun getAllTasks(): Call<TaskResponse>

    @GET("tasks/{category}")
    fun getTasksByCategory(@Path("category") category: String): Call<TaskResponse>

    @POST("tasks")
    fun addTask(@Body task: Task): Call<Task>

    @PUT("task/{id}")
    fun updateTask(@Path("id") id: Int, @Body task: Task): Call<Task>

    @PATCH("task/{id}")
    fun updateStatus(
        @Path("id") id: Int,
        @Body status: Map<String, String>
    ): Call<Task>

    @DELETE("task/{id}")
    fun deleteTask(@Path("id") id: Int): Call<Void>
}