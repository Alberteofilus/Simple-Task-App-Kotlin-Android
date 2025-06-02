# Simple-Task-App-Kotlin-Android
Simple Task App is a basic Android application that fetches and displays a list of tasks from a Flask + MySQL backend using a REST API. Built with Kotlin , it presents task data in a simple ListView interface.

## Key Features

✅ **Task Management**:

* Create, read, update, delete tasks
* Categorize tasks (Important, Urgent, Regular)
* Mark tasks as completed

✅ **User Interface**:

* Task list displayed in card layout
* Filter tasks by category
* Show task count per category

✅ **Technologies**:

* **Frontend**: Android (Kotlin)
* **Backend**: Flask (Python)
* **Database**: MySQL
* **Architecture**: Client-Server with REST API

## Application Components

### Android Client

* Uses Retrofit for API communication
* Full CRUD implementation
* Optimistic UI updates
* Parcelable data model

### Flask Backend

* RESTful API endpoints
* Soft delete system
* Input validation
* Consistent response format

## How to Run

### Backend

1. Install dependencies: `pip install -r requirements.txt`
2. Set up MySQL database
3. Run the server: `python app.py`

### Android

1. Open the project in Android Studio
2. Adjust `BASE_URL` in `ApiClient.kt`
3. Build and run on emulator/device
