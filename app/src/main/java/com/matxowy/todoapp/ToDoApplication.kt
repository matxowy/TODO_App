package com.matxowy.todoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // ustawiamy by aktywować dagger hilta
class ToDoApplication : Application() {
}