package com.rafd.levanf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.R.color.cardview_dark_background
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

/**
 * Radial_LinealActivity es la actividad que permite a los usuarios gestionar tramos radiales y lineales.
 * Utiliza Firebase Realtime Database para guardar y cargar tramos.
 */
class Radial_LinealActivity : AppCompatActivity() {

    // Referencia a la base de datos de Firebase
    private val userRef = Firebase.database.getReference("Usuarios")

    // UUID del usuario y del tramo
    private lateinit var uuid: String
    private lateinit var uuidTramo: String

    // Lanzador de actividad para resultados
    private lateinit var resultLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    // Lista para almacenar los tramos
    private val tramos = ArrayList<Tramo>()

    // Vista de lista y adaptador para los tramos
    private lateinit var listView: ListView
    private lateinit var tramoAdapter: TramoAdapter

    // Comportamiento del BottomSheet
    private lateinit var behaviour: BottomSheetBehavior<LinearLayout>

    /**
     * Método llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners de eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_radial_lineal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura el lanzador de actividad para resultados
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val returnedString = data?.getStringExtra("resultKey")
                uuidTramo = returnedString.toString()
                cargarTramos(uuidTramo)
            }
        }

        // Obtiene el UUID del usuario de los extras del Intent
        uuid = intent.extras?.getString("uuid") ?: "no uuid"

        // Configura la toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Permite controlar el estado del BottomSheet
        val bottomSheetTotal = findViewById<LinearLayout>(R.id.bottomSheet)
        behaviour = BottomSheetBehavior.from(bottomSheetTotal)

        // Configura la lista de tramos y su adaptador
        listView = findViewById(R.id.lista)
        tramoAdapter = TramoAdapter(this, tramos)
        listView.adapter = tramoAdapter

        // Configura el listener para el botón de generar gráficas
        findViewById<Button>(R.id.generar).setOnClickListener {
            val intent = Intent(this, GraphsActivity::class.java)
            intent.putExtra("tramos", tramos)
            intent.putExtra("rpm", findViewById<EditText>(R.id.etVelocidad).text.toString().toDouble())
            startActivity(intent)
        }

        // Configura el listener para agregar tramos
        findViewById<TextView>(R.id.agregarTramos).setOnClickListener {
            tramos.add(Tramo())
            tramoAdapter.notifyDataSetChanged()
        }

        // Configura el listener para limpiar tramos
        findViewById<ImageView>(R.id.limpiarTramos).setOnClickListener {
            tramos.clear()
            tramoAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Tramos borrados!", Toast.LENGTH_SHORT).show()
        }

        // Configura el listener para guardar tramos
        findViewById<ImageView>(R.id.guardarTramo).setOnClickListener {
            guardarTramoGeneral()
        }

        // Configura el listener para cargar tramos
        findViewById<ImageView>(R.id.cargar).setOnClickListener {
            resultLauncher.launch(Intent(this, TramosGuardadosActivity::class.java).putExtra("uuid", uuid))
        }

        // Configura el listener para cambios en la velocidad
        findViewById<EditText>(R.id.etVelocidad).doOnTextChanged { text, _, _, _ ->
            verificarDatos()
        }
    }

    /**
     * Verifica que los datos ingresados sean válidos y habilita el botón para graficar en caso
     * de que lo sean.
     */
    fun verificarDatos() {
        if (datosValidos()) {
            findViewById<Button>(R.id.generar).isEnabled = true
            findViewById<Button>(R.id.generar).setBackgroundColor(Color.parseColor("#7B1FA2"))
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            findViewById<Button>(R.id.generar).isEnabled = true
            findViewById<Button>(R.id.generar).setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    cardview_dark_background
                )
            )
            behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    /**
     * Regresa true si todos los campos necesarios para realizar las operaciones son rellenados con
     * datos correctos, false en caso contrario.
     */
    fun datosValidos(): Boolean {
        var alturaAcumulada = 0.0
        var alturasValidas = true
        var segmentosValidos = true

        for (tramo in tramos) {
            val altura = tramo.altura.toDoubleOrNull() ?: 0.0
            val segmento = tramo.segmento.lowercase()

            when (segmento) {
                "subida" -> {
                    alturasValidas = alturasValidas && altura > 0.0
                    alturaAcumulada += altura
                }
                "bajada" -> {
                    alturasValidas = alturasValidas && altura > 0.0
                    alturaAcumulada -= altura
                }
                "det. alto" -> {
                }
                "det. bajo" -> {
                }
            }
        }

        return 360 == tramos.sumOf { it.ejeX.toIntOrNull() ?: 0 }
            .apply { findViewById<TextView>(R.id.total).text = this.toString() + " " }
                && alturaAcumulada == 0.0
                && findViewById<EditText>(R.id.etVelocidad).text.toString().isNotEmpty()
                && alturasValidas
                && segmentosValidos
    }

    /**
     * Guarda los tramos en la base de datos.
     */
    private fun guardarTramoGeneral() {
        userRef.child(uuid).child("Tramos").push().setValue(tramos).addOnSuccessListener {
            Toast.makeText(this, "Tramos guardados!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Carga los tramos desde la base de datos.
     * @param tramosuuid El UUID del tramo a cargar.
     */
    fun cargarTramos(tramosuuid: String) {
        tramos.clear()
        userRef.child(uuid).child("Tramos").child(tramosuuid).addValueEventListener(object : ValueEventListener {
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
                    tramoAdapter.notifyDataSetChanged()
                }

            override fun onCancelled(error: DatabaseError) = error.toException().printStackTrace()
        })
    }

    /**
     * Adaptador personalizado para la lista de tramos.
     */
    inner class TramoAdapter(context: Context, private val tramos: ArrayList<Tramo>) :
        ArrayAdapter<Tramo>(context, 0, tramos) {

        /**
         * Método para obtener la vista de un elemento de la lista.
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

            view.findViewById<ImageView>(R.id.limpiar).setOnClickListener {
                tramos.removeAt(position)
                notifyDataSetChanged()
            }

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
                verificarDatos()
            }

            // Manejar cambios en altura
            altura.doOnTextChanged { text, _, _, _ ->
                val tramoIndex = view.tag as? Int ?: return@doOnTextChanged
                tramos[tramoIndex].altura = text.toString()
                verificarDatos()
            }

            // Manejar cambios en ecuación
            ecuacion.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val tramoIndex = view.tag as? Int ?: return@OnItemClickListener
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    tramos[tramoIndex].ecuacion = selectedItem
                    verificarDatos()
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
    data class Tramo(
        var segmento: String = "Subida",
        var ejeX: String = "",
        var ecuacion: String = "Cicloidal",
        var altura: String = ""
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(segmento)
            parcel.writeString(ejeX)
            parcel.writeString(ecuacion)
            parcel.writeString(altura)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Tramo> {
            override fun createFromParcel(parcel: Parcel): Tramo {
                return Tramo(parcel)
            }

            override fun newArray(size: Int): Array<Tramo?> {
                return arrayOfNulls(size)
            }
        }
    }
}
