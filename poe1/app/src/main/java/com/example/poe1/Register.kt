package com.example.poe1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var edLogUsername: EditText
    lateinit var edLogPassword: EditText
    lateinit var logBtn: Button
    lateinit var regTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        regTv = findViewById(R.id.tvNotAUser)
        logBtn = findViewById(R.id.btnLog)
        edLogUsername = findViewById(R.id.edtUsername)
        edLogPassword = findViewById(R.id.edtPass)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
//method
        regTv.setOnClickListener {
            val intent4 = Intent(this@Register, Login::class.java)
            startActivity(intent4)
        }
        logBtn.setOnClickListener {
            val username = edLogUsername.text.toString().trim()
            val password = edLogPassword.text.toString().trim()

            //checks
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this@Register,
                    "Email or password cant be empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }// if ends
            loginUser(username, password)
        }// listener ends
    }// on create ends

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    intent = Intent(this@Register, Home::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@Register, "Invalid login", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

