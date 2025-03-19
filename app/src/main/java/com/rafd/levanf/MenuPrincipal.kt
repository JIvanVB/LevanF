package com.rafd.levanf

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.rafd.levanf.databinding.ActivityMenuPrincipalBinding

class MenuPrincipal : AppCompatActivity() {
    private lateinit var binding: ActivityMenuPrincipalBinding
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        email = intent.extras?.getString("name") ?: "Default Email"

        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<TextView>(R.id.userid).text = email

        findViewById<Button>(R.id.logout).setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, MainActivity::class.java).apply { setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }

        binding.btnRadial.setOnClickListener {
            val intent = Intent(this, MenuRadial::class.java)
            startActivity(intent)
        }
    }
}