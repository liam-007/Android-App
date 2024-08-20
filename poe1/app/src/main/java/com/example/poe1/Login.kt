package com.example.poe1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var edName: EditText
    lateinit var edEmail: EditText
    lateinit var edPass: EditText
    lateinit var edConfPass: EditText
    lateinit var btnReg: Button
    lateinit var regTv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        edName = findViewById(R.id.edRegName)
        edEmail = findViewById(R.id.edRegEmail)
        edPass = findViewById(R.id.edRegPass)
        edConfPass = findViewById(R.id.edRegConfPass)
        btnReg = findViewById(R.id.btnSignUp)
        regTv = findViewById(R.id.tvRegistered)
        auth = FirebaseAuth.getInstance()

        regTv.setOnClickListener {
            val intent3 = Intent(this@Login, Register::class.java)
            startActivity(intent3)
        }

        btnReg.setOnClickListener {
            val name = edName.text.toString().trim()
            val email = edEmail.text.toString().trim()
            val password = edPass.text.toString().trim()
            val confirmPassword = edConfPass.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Login, Register::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Registration failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }
}