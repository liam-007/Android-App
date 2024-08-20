package com.example.poe1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar

class MainActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 5000 // 5 seconds delay
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the progress bar
        progressBar = findViewById(R.id.progressBar)

        // Show the progress bar
        progressBar.visibility = View.VISIBLE

        // Delay for 5 seconds and then navigate to the About activity
        Handler().postDelayed({
            // Hide the progress bar
            progressBar.visibility = View.GONE

            startActivity(Intent(this, About::class.java))
            finish() // Finish the splash activity so that it's not accessible via back button

        }, SPLASH_DELAY)
    }
}