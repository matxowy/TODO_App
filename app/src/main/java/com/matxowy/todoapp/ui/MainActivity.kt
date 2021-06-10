package com.matxowy.todoapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.matxowy.todoapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // oznaczamy klasę by można było wstrzykiwać
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}