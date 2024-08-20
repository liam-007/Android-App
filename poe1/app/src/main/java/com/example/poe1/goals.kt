package com.example.poe1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class goals : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var btnSave: Button
    lateinit var btnClear: Button
    lateinit var etMinHours: EditText
    lateinit var etMaxHours: EditText
    lateinit var tvgoals: TextView
    lateinit var btnBack:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        auth = Firebase.auth
        btnClear = findViewById(R.id.btnClear)
        btnSave = findViewById(R.id.btnSave)
        etMinHours = findViewById(R.id.etMinHours)
        tvgoals = findViewById(R.id.tvgoals)
        etMaxHours = findViewById(R.id.etMaxHours)
        btnBack = findViewById(R.id.btnBack)
        btnSave.setOnClickListener {



            btnBack.setOnClickListener {
                finish() // Close the current activity and return to the previous one
            }

            val minHours = etMinHours.text.toString().toIntOrNull()
            val maxHours = etMaxHours.text.toString().toIntOrNull()




            if (minHours != null && maxHours != null) {
                // Update UI to display the goals for the day
                displayGoals(minHours, maxHours)
            } else {
                // Handle invalid input
                // For example, show a Toast message
                // Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
            }
        }

        btnClear.setOnClickListener {
            clearGoals()
        }
    }
    private fun clearGoals() {
        // Clear the EditText fields and TextView
        etMinHours.text.clear()
        etMaxHours.text.clear()
        tvgoals.text = ""
    }

    private fun displayGoals(minHours: Int, maxHours: Int) {
        // Display the goals in the TextView
        val goalsText = "Your goals for the day:\nMinimum Hours: $minHours\nMaximum Hours: $maxHours"
        tvgoals.text = goalsText
    }
}
