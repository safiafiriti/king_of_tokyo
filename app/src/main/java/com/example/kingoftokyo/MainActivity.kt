package com.example.kingoftokyo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.view.WindowInsetsController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Change the statusBar of the window with this color (background color of the fragment)
        // we can later on , make it better by making it take the background color of the current shown Fragment !
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_background)
    }
}