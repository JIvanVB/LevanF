package com.rafd.levanf

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.rafd.levanf.databinding.ActivityMenuRadialBinding

class MenuRadial : AppCompatActivity() {
    private lateinit var binding: ActivityMenuRadialBinding
    private lateinit var uuid: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuRadialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uuid = intent.extras?.getString("uuid") ?: "Default Email"

        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MenuPrincipal::class.java)
            startActivity(intent)
        }

        binding.btnLineal.setOnClickListener {
            val intent = Intent(this, Radial_LinealActivity::class.java).putExtra("uuid",uuid)
            startActivity(intent)
        }
    }
}