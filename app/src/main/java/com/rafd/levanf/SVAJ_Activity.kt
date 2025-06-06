package com.rafd.levanf

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
/**
 * SVAJ_Activity esta por implementarse eventualmente posterior mente.
 * muestra las graficas generadas por la calculadora de SVAJ.
 */
class SVAJ_Activity : AppCompatActivity() {

    /**
     * Metodo llamado cuando se crea la actividad.
     * Esta por implementarse eventualmente posterior mente
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_svaj)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}