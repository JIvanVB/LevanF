package com.rafd.levanf

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.rafd.levanf.databinding.ActivityMenuPrincipalBinding

class MenuPrincipal : AppCompatActivity() {
    private lateinit var binding: ActivityMenuPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRadial.setOnClickListener {
            val intent = Intent(this, MenuRadial::class.java)
            startActivity(intent)
        }
    }
}