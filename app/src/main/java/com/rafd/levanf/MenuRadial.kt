package com.rafd.levanf

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.rafd.levanf.databinding.ActivityMenuRadialBinding

/**
 * MenuRadial es la actividad que maneja el menú radial de la aplicación.
 * Proporciona opciones para navegar a diferentes actividades relacionadas con el menú radial.
 */
class MenuRadial : AppCompatActivity() {

    // Binding para la vista de la actividad
    private lateinit var binding: ActivityMenuRadialBinding

    // UUID del usuario
    private lateinit var uuid: String

    /**
     * Metodo llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners de eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configura el binding para la vista de la actividad
        binding = ActivityMenuRadialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtiene el UUID del usuario de los extras del Intent
        uuid = intent.extras?.getString("uuid") ?: "Default Email"

        // Configura el listener para el botón de navegación de la toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Configura el listener para el botón de actividad lineal pasan el uuid para uso posterior
        binding.btnLineal.setOnClickListener {
            val intent = Intent(this, Radial_LinealActivity::class.java).putExtra("uuid", uuid)
            startActivity(intent)
        }
    }
}
