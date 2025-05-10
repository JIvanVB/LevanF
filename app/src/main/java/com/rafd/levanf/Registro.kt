package com.rafd.levanf

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Registro es la actividad que maneja el registro de nuevos usuarios en la aplicación.
 * Utiliza Firebase Authentication para crear cuentas de usuario y Firebase Realtime Database para almacenar información adicional del usuario.
 */
class Registro : AppCompatActivity() {

    // Instancia de Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Referencia a la base de datos de Firebase
    private val userRef = Firebase.database.getReference("Usuarios")

    /**
     * Metodo llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners de eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa Firebase Authentication
        auth = Firebase.auth

        // Referencias a los elementos de la interfaz de usuario
        val email = findViewById<EditText>(R.id.etrEmail)
        val password = findViewById<EditText>(R.id.etrPassword)
        val confirmPassword = findViewById<EditText>(R.id.etrConfirmPassword)
        val errorTv = findViewById<TextView>(R.id.Error)
        val button = findViewById<Button>(R.id.btnRegistrar)

        // Configura el listener para el botón de registro
        button.setOnClickListener {
            when {
                email.text.isEmpty() ||
                        password.text.isEmpty() ||
                        confirmPassword.text.isEmpty() -> {
                    errorTv.text = "Todos los campos deben de ser llenados"
                    errorTv.visibility = View.VISIBLE
                }
                password.text.toString() != confirmPassword.text.toString() -> {
                    errorTv.text = "Las contraseñas no coinciden"
                    errorTv.visibility = View.VISIBLE
                }
                password.text.toString().length < 6 -> {
                    errorTv.text = "La contraseña debe de tener al menos 6 caracteres"
                    errorTv.visibility = View.VISIBLE
                }
                else -> {
                    errorTv.visibility = View.INVISIBLE
                    signIn(email.text.toString(), password.text.toString())
                }
            }
        }
    }

    /**
     * Registra un nuevo usuario con el correo electrónico y la contraseña proporcionados.
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     */
    fun signIn(email: String, password: String) {
        Log.d("TAG", "msg: email: $email, password: $password")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "signUpWithEmail:success")
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    guardarDatos(email)
                    startActivity(intent)
                } else {
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Guarda la información adicional del usuario en la base de datos.
     * @param string El correo electrónico del usuario.
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun guardarDatos(string: String) {
        val valores = hashMapOf<String, String>(
            "nombre" to findViewById<EditText>(R.id.etNombre).text.toString().trim(),
            "apellido" to findViewById<EditText>(R.id.etApellido).text.toString().trim(),
            "email" to findViewById<EditText>(R.id.etrEmail).text.toString().trim()
        )
        val u = Uuid.random().toString()
        userRef.child(u).setValue(valores)
    }
}
