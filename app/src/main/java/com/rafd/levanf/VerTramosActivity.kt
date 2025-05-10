package com.rafd.levanf

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

/**
 * VerTramosActivity es la actividad que muestra los detalles de un tramo específico.
 * Utiliza Firebase Realtime Database para recuperar los detalles del tramo.
 */
class VerTramosActivity : AppCompatActivity() {

    // Lista para almacenar los tramos
    private val tramos = ArrayList<Tramo>()

    // Referencia a la base de datos de Firebase
    private val userRef = Firebase.database.getReference("Usuarios")

    // UUID del usuario y el tramo seleccionado
    private lateinit var uuid: String
    private lateinit var eltramo: String

    /**
     * Metodo llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners de eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_tramos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtiene el UUID del usuario y el tramo seleccionado de los extras del Intent
        uuid = intent.extras?.getString("uuid") ?: "no uuid"
        eltramo = intent.extras?.getString("eltramo") ?: "no tramo"
        val t = userRef.child(uuid).child("Tramos").child(eltramo)

        // Configura la toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Configura el adaptador para la lista de tramos
        val tramoadapter = TramoAdapter(this, tramos)

        // Configura un listener para obtener los detalles del tramo desde la base de datos
        t.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) =
                data.let {
                    it.children.forEach {
                        tramos.add(
                            Tramo(
                                it.child("segmento").value.toString(),
                                it.child("ejeX").value.toString(),
                                it.child("ecuacion").value.toString(),
                                it.child("altura").value.toString()
                            )
                        )
                    }
                    tramoadapter.notifyDataSetChanged()
                }

            override fun onCancelled(error: DatabaseError) = error.toException().printStackTrace()
        })

        // Configura el texto del tramo en la interfaz de usuario
        findViewById<TextView>(R.id.conjunto).text = eltramo

        // Configura el adaptador para la lista de tramos
        findViewById<ListView>(R.id.lista).adapter = tramoadapter
    }

    /**
     * Adaptador personalizado para la lista de tramos.
     */
    inner class TramoAdapter(context: Context, private val tramos: ArrayList<Tramo>) :
        ArrayAdapter<Tramo>(context, 0, tramos) {

        /**
         * Metodo para obtener la vista de un elemento de la lista.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view =
                convertView ?: LayoutInflater.from(context).inflate(R.layout.tramo, parent, false)

            val tramo = tramos[position]

            val segmento: AutoCompleteTextView = view.findViewById(R.id.segmentos)
            val ejeXText: TextView = view.findViewById(R.id.eje)
            val ecuacion: AutoCompleteTextView = view.findViewById(R.id.ecuaciones)
            val altura: TextView = view.findViewById(R.id.altura)
            val ec = view.findViewById<TextInputLayout>(R.id.ecuacionelo)

            view.findViewById<ImageView>(R.id.limpiar).visibility = View.GONE

            // Asignar valores iniciales
            segmento.setText(tramo.segmento, false)
            ejeXText.text = tramo.ejeX
            ecuacion.setText(tramo.ecuacion, false)
            altura.text = tramo.altura

            view.tag = position

            // Configurar adaptadores
            segmento.setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.item_op,
                    resources.getStringArray(R.array.segmentos)
                )
            )
            ecuacion.setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.item_op,
                    resources.getStringArray(R.array.ecuaciones)
                )
            )

            // Manejar cambios en segmento
            segmento.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val tramoIndex = view.tag as? Int ?: return@OnItemClickListener
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    tramos[tramoIndex].segmento = selectedItem
                    val ec = view.findViewById<TextInputLayout>(R.id.ecuacionelo)
                    when (selectedItem) {
                        "Det. Alto", "Det. Bajo" -> ec.visibility = View.INVISIBLE
                        else -> ec.visibility = View.VISIBLE
                    }
                }

            // Manejar cambios en ejeX
            ejeXText.doOnTextChanged { text, _, _, _ ->
                val tramoIndex = view.tag as? Int ?: return@doOnTextChanged
                tramos[tramoIndex].ejeX = text.toString()
            }

            // Manejar cambios en altura
            altura.doOnTextChanged { text, _, _, _ ->
                val tramoIndex = view.tag as? Int ?: return@doOnTextChanged
                tramos[tramoIndex].altura = text.toString()
            }

            // Manejar cambios en ecuación
            ecuacion.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val tramoIndex = view.tag as? Int ?: return@OnItemClickListener
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    tramos[tramoIndex].ecuacion = selectedItem
                }

            if (segmento.text.toString() == "Det. Alto" || segmento.text.toString() == "Det. Bajo") {
                ec.visibility = View.INVISIBLE
            } else {
                ec.visibility = View.VISIBLE
            }

            return view
        }
    }

    /**
     * Clase que representa un tramo.
     */
    inner class Tramo(
        var segmento: String = "Subida",
        var ejeX: String = "",
        var ecuacion: String = "Cicloidal",
        var altura: String = ""
    )
}
