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

class Radial_LinealActivity : AppCompatActivity() {

    private val userRef = Firebase.database.getReference("Usuarios")
    private lateinit var uuid: String
    private lateinit var uuidTramo: String
    private lateinit var resultLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private val tramos = ArrayList<Tramo>()
    private lateinit var listView: ListView
    private lateinit var tramoAdapter: TramoAdapter
    private lateinit var behaviour: BottomSheetBehavior<LinearLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_radial_lineal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); v.setPadding(
            systemBars.left,
            systemBars.top,
            systemBars.right,
            systemBars.bottom
        ); insets
        }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val returnedString = data?.getStringExtra("resultKey")
                uuidTramo=returnedString.toString()
                cargarTramos(uuidTramo)
            }
        }

        uuid = intent.extras?.getString("uuid") ?: "no uuid"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Permite controlar el estado del BottomSheet
        val bottomSheetTotal = findViewById<LinearLayout>(R.id.bottomSheet)
        behaviour = BottomSheetBehavior.from(bottomSheetTotal)

        listView = findViewById(R.id.lista)
        tramoAdapter = TramoAdapter(this, tramos)
        listView.adapter = tramoAdapter

        // Envia los tramos y la velocidad a la pantalla que genera las gráficas
        findViewById<Button>(R.id.generar).setOnClickListener {
            val intent = Intent(this, GraphsActivity::class.java)
            intent.putExtra("tramos", tramos)
            intent.putExtra("rpm", findViewById<EditText>(R.id.etVelocidad).text.toString().toDouble())
            startActivity(intent)
        }

        findViewById<TextView>(R.id.agregarTramos).setOnClickListener {
            tramos.add( Tramo())
            tramoAdapter.notifyDataSetChanged()
        }

        findViewById<ImageView>(R.id.limpiarTramos).setOnClickListener {
            tramos.clear()
            tramoAdapter.notifyDataSetChanged()
            Toast.makeText(this,"Tramos borrados!",Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.guardarTramo).setOnClickListener {
            guardarTramoGeneral()
        }

        findViewById<ImageView>(R.id.cargar).setOnClickListener {
            resultLauncher.launch(Intent(this, TramosGuardadosActivity::class.java).putExtra("uuid",uuid))
        }

        findViewById<EditText>(R.id.etVelocidad).doOnTextChanged { text, _, _, _ ->
            verificarDatos()
        }
    }

    /**
     * Verifica que los datos ingresados sean validos y habilita el boton para graficar en caso
     * de que lo sean
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
     * datos correctos, false en caso contrario
     */
    fun datosValidos(): Boolean {
        var alturaAcumulada = 0.0
        var alturasValidas = true
        var segmentosValidos = true

        //var segmentoAnterior = ""
        for (tramo in tramos) {
            val altura = tramo.altura.toDoubleOrNull()?: 0.0
            val segmento = tramo.segmento.lowercase()

            when (segmento) {
                "subida" -> {
                    alturasValidas = alturasValidas && altura > 0.0
                    //segmentosValidos = segmentosValidos && segmentoAnterior.isNotEmpty() && segmentoAnterior != "det. bajo"
                    alturaAcumulada += altura
                }
                "bajada" -> {
                    alturasValidas = alturasValidas && altura > 0.0
                    //segmentosValidos = segmentosValidos && segmentoAnterior.isNotEmpty() && segmentoAnterior != "det. alto"
                            //&& alturaAcumulada != 0.0
                    alturaAcumulada -= altura
                }
                "det. alto" -> {
                    //segmentosValidos = segmentosValidos && segmentoAnterior.isNotEmpty() && segmentoAnterior != "det. alto"
                            //&& segmentoAnterior != "det. bajo" && alturaAcumulada != 0.0
                }
                "det. bajo" -> {
                    //segmentosValidos = segmentosValidos && segmentoAnterior.isNotEmpty() && segmentoAnterior != "det. alto"
                            //&& segmentoAnterior != "det. bajo" && alturaAcumulada == 0.0
                }
            }
            //segmentoAnterior = segmento
        }

        return 360 == tramos.sumOf { it.ejeX.toIntOrNull() ?: 0 }
             .apply { findViewById<TextView>(R.id.total).text = this.toString() + " " }
                && alturaAcumulada == 0.0
                && findViewById<EditText>(R.id.etVelocidad).text.toString().isNotEmpty()
                && alturasValidas
                && segmentosValidos
    }

    private fun Radial_LinealActivity.guardarTramoGeneral() {
        //tramos.forEach { Log.d("Tramo", "Segmento: ${it.segmento}, Eje X: ${it.ejeX}, Ecuación: ${it.ecuacion}, Altura: ${it.altura}") }
        userRef.child(uuid).child("Tramos").push().setValue(tramos).addOnSuccessListener {
            Toast.makeText(this,"Tramos guardados!",Toast.LENGTH_SHORT).show()
        }
    }

    fun Radial_LinealActivity.cargarTramos(tramosuuid: String) {
        tramos.clear()
        userRef.child(uuid).child("Tramos").child(tramosuuid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) =
                data.let {
                    it.children.forEach {
                        tramos.add(Tramo(
                            it.child("segmento").value.toString(),
                            it.child("ejeX").value.toString(),
                            it.child("ecuacion").value.toString(),
                            it.child("altura").value.toString()
                        ))
                    }
                    tramoAdapter.notifyDataSetChanged()
                }
            override fun onCancelled(error: DatabaseError) = error.toException().printStackTrace()
        })
    }

    inner class TramoAdapter(context: Context, private val tramos: ArrayList<Tramo>) :
        ArrayAdapter<Tramo>(context, 0, tramos) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view =
                convertView ?: LayoutInflater.from(context).inflate(R.layout.tramo, parent, false)

            val tramo = tramos[position]

            val segmento: AutoCompleteTextView = view.findViewById(R.id.segmentos)
            val ejeXText: TextView = view.findViewById(R.id.eje)
            val ecuacion: AutoCompleteTextView = view.findViewById(R.id.ecuaciones)
            val altura: TextView = view.findViewById(R.id.altura)
            val ec = view.findViewById<TextInputLayout>(R.id.ecuacionelo)

            view.findViewById<ImageView>(R.id.limpiar).setOnClickListener { tramos.removeAt(position);
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

            if(segmento.text.toString() == "Det. Alto" || segmento.text.toString() == "Det. Bajo"){

                ec.visibility = View.INVISIBLE
            }else{
                ec.visibility = View.VISIBLE
            }

            return view
        }
    }


    data class Tramo (
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