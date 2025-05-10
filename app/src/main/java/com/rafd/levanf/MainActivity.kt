package com.rafd.levanf

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

/**
 * MainActivity es la actividad principal con la que inicia la aplicación.
 * Maneja la autenticación de usuarios utilizando Firebase Authentication.
 */
class MainActivity : AppCompatActivity() {

    // Instancia de Firebase Authentication
    private lateinit var auth: FirebaseAuth

    /**
     * Metodo llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners de eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa Firebase Authentication
        auth = Firebase.auth

        // Referencias a los elementos de la interfaz de usuario
        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnEntrar = findViewById<Button>(R.id.btnLogin)

        // Configura el listener para el botón de inicio de sesión
        btnEntrar.setOnClickListener {
            login(email.text.toString(), password.text.toString())
        }

        // Configura el listener para el texto de registro
        findViewById<TextView>(R.id.tvregistro).setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }
    }

    /**
     * Navega al menú principal de la aplicación.
     * @param context El contexto de la aplicación.
     * @param user El usuario autenticado.
     */
    fun goToMenuPrincipal(context: Context, user: FirebaseUser) {
        val intent = Intent(context, MenuPrincipal::class.java)
        intent.putExtra("name", user.email.toString())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    /**
     * Muestra un mensaje de error en la interfaz de usuario.
     * @param text El texto del mensaje de error.
     * @param visible Indica si el mensaje de error debe ser visible.
     */
    fun showError(text: String = "", visible: Boolean) {
        val errorTv: TextView = findViewById(R.id.tvError)
        errorTv.text = text
        errorTv.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Metodo llamado cuando la actividad se está iniciando.
     * Verifica si hay un usuario autenticado y navega al menú principal si es así.
     */
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToMenuPrincipal(this, currentUser)
        }
    }

    /**
     * Intenta iniciar sesión con el correo electrónico y la contraseña proporcionados.
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     */
    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    showError(visible = false)
                    goToMenuPrincipal(this, user!!)
                } else {
                    showError(text = "Usuario y/o contraseña equivocados", visible = true)
                }
            }
    }
}
