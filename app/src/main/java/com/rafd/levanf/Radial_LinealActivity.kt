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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


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

        val grafica = findViewById<LineChart>(R.id.graficaPruebas)

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
    }

    fun suma() {
        if (360 == tramos.sumOf { it.ejeX.toIntOrNull() ?: 0 }
                .apply { findViewById<TextView>(R.id.total).text = this.toString() + " " }) {
            findViewById<Button>(R.id.generar).isEnabled = true
            findViewById<Button>(R.id.generar).setBackgroundColor(Color.parseColor("#7B1FA2"))
            val etVelocidad = findViewById<EditText>(R.id.etVelocidad)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            val entries = calcularVelocidad(tramos[0].altura.toFloatOrNull()?: 5f, tramos[0].ejeX.toIntOrNull()?: 90,
                etVelocidad.text.toString().toIntOrNull()?: 200)
            graficarSubida(entries)
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

    fun graficarSubida(entries: ArrayList<Entry>) {
        val dataSet = LineDataSet(entries, "Desplazamiento s (mm)").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 3f
            setDrawCircles(false)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        val grafica = findViewById<LineChart>(R.id.graficaPruebas)
        grafica.data = lineData
        grafica.invalidate()
    }

    fun calcularSubida(altura: Float, beta: Int): ArrayList<Entry> {
        val teta = ArrayList<Float>()
        val entries = ArrayList<Entry>()

        for (x in 1..beta) {
            teta.add(x.toFloat())
        }

        for (x in teta) {
            val y = altura * ((x / beta) - ((1 / (2 * PI)) * sin(2 * PI * (x / beta))))
            entries.add(Entry(x, y.toFloat()))
        }

        return entries
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

    fun calcularVelocidad(altura: Float, beta: Int, rpm: Int): ArrayList<Entry> {
        val teta = ArrayList<Double>()
        val entries = ArrayList<Entry>()
        val w = rpm.aRadianesSegundos();

        for (x in 1..beta) {
            teta.add(x.aRadianes())
        }

        for (x in teta) {
            val y = ((altura / beta.aRadianes()) * (1 - cos(2 * PI * (x / beta.aRadianes())))) * w
            entries.add(Entry(x.toFloat(), y.toFloat()))
        }

        return entries
    }

    fun Int.aRadianesSegundos(): Double {
        return this * 2 * Math.PI / 60
    }

    fun Int.aRadianes(): Double {
        return this * Math.PI / 180
    }

    fun calcularAceleracion(altura: Float, beta: Int, rpm: Int): ArrayList<Entry> {
        val teta = ArrayList<Float>()
        val entries = ArrayList<Entry>()

        for (x in 1..beta) {
            teta.add(x.toFloat())
        }

        for (x in teta) {
            val y = 2 * PI * (altura / beta.toDouble().pow(2)) * sin(2 * PI * (x / beta)) * rpm.toDouble().pow(2)
            entries.add(Entry(x, y.toFloat()))
        }

        return entries
    }



    fun calcularSacudimiento(altura: Float, beta: Int, rpm: Int): ArrayList<Entry> {
        val teta = ArrayList<Float>()
        val entries = ArrayList<Entry>()

        for (x in 1..beta) {
            teta.add(x.toFloat())
        }

        for (x in teta) {
            val y = 4 * PI.pow(2) * (altura / beta.toDouble().pow(3)) * cos(2 * PI * (x / beta)) * rpm.toDouble().pow(3)
            entries.add(Entry(x, y.toFloat()))
        }

        return entries
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
                suma()
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


