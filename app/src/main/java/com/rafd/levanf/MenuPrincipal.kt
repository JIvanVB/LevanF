package com.rafd.levanf

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.rafd.levanf.databinding.ActivityMenuPrincipalBinding

class MenuPrincipal : AppCompatActivity() {
    private lateinit var binding: ActivityMenuPrincipalBinding
    private lateinit var email: String
    private lateinit var uuid: String
    private val userRef = Firebase.database.getReference("Usuarios")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        email = intent.extras?.getString("name") ?: "Default Email"

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                data.let{
                    it.children.forEach { u ->
                        val ik = u.key.toString()
                        val e = email
                        val n = u.child("email").value.toString()
                        if (n==e)
                            uuid=ik
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) = error.toException().printStackTrace()
        })



        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<TextView>(R.id.userid).text = email.substringBefore('@')

        findViewById<Button>(R.id.logout).setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, MainActivity::class.java).apply { setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }

        binding.btnRadial.setOnClickListener {
            val intent = Intent(this, MenuRadial::class.java).putExtra("uuid",uuid)
            startActivity(intent)
        }
    }
}