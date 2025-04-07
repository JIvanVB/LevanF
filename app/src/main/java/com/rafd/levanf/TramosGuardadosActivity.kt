package com.rafd.levanf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class TramosGuardadosActivity : AppCompatActivity() {

    private val tramosConjuntos = ArrayList<String>()
    private val userRef = Firebase.database.getReference("Usuarios")
    private lateinit var uuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tramos_guardados)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        uuid = intent.extras?.getString("uuid") ?: "no uuid"
        val t = userRef.child(uuid).child("Tramos")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        t.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) =
                data.let {
                    it.children.forEach {
                        tramosConjuntos.add(it.key.toString())
                    }
                }
            override fun onCancelled(error: DatabaseError) = error.toException().printStackTrace()
        })
        findViewById<Button>(R.id.confirmar).setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("resultKey", findViewById<Button>(R.id.confirmar).text.toString())
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        findViewById<ListView>(R.id.lista).adapter = tadapter(this, tramosConjuntos)
        findViewById<ListView>(R.id.lista).setOnItemClickListener{ _, _, position, _ ->
            findViewById<Button>(R.id.confirmar).text=tramosConjuntos[position].toString()
            findViewById<Button>(R.id.confirmar).visibility=View.VISIBLE
            startActivity(Intent(this, VerTramosActivity::class.java).putExtra("uuid",uuid).putExtra("eltramo",tramosConjuntos[position]))
        }

    }

    inner class tadapter(context: Context, private val tramosConjuntos: ArrayList<String>) :
        ArrayAdapter<String>(context, 0, tramosConjuntos) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view =convertView ?: LayoutInflater.from(context).inflate(R.layout.tramo_conjunto, parent, false)

            view.findViewById<TextView>(R.id.suid).text = tramosConjuntos[position]

            return view
        }

    }
}