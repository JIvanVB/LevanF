package com.rafd.levanf

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.rafd.levanf.databinding.ActivityMenuPrincipalBinding

/**
 * MenuPrincipal es la actividad principal del menú de la aplicación.
 * Maneja la visualización del menú principal y la interacción con Firebase Realtime Database.
 */
class MenuPrincipal : AppCompatActivity() {

    // Binding para la vista de la actividad
    private lateinit var binding: ActivityMenuPrincipalBinding

    // Correo electrónico del usuario
    private lateinit var email: String

    // UUID del usuario
    private var uuid: String? = null

    // Referencia a la base de datos de Firebase
    private val userRef = Firebase.database.getReference("Usuarios")

    /**
     * Metodo llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners de eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Obtiene el correo electrónico del usuario de los extras del Intent
        email = intent.extras?.getString("name") ?: "Default Email"

        // Configura el binding para la vista de la actividad
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura el texto del usuario en la interfaz de usuario
        findViewById<TextView>(R.id.userid).text = email.substringBefore('@')

        // Configura el listener para el botón de cerrar sesión
        findViewById<Button>(R.id.logout).setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, MainActivity::class.java).apply { setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }

        // Configura un listener para obtener el UUID del usuario desde la base de datos
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                data.let {
                    it.children.forEach { u ->
                        val ik = u.key.toString()
                        val e = email
                        val n = u.child("email").value.toString()
                        if (n == e) {
                            uuid = ik
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) = error.toException().printStackTrace()
        })

        // Configura el listener para el botón de menú radial pasando el uuid para uso posterior
        binding.btnRadial.setOnClickListener {
            if (uuid == null)
                Toast.makeText(this, "Recuperando uuid, espere y vuelva  intentar", Toast.LENGTH_SHORT).show()
            else
                startActivity(Intent(this, MenuRadial::class.java).putExtra("uuid", uuid))

        }
    }
}
