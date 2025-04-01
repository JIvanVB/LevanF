package com.rafd.levanf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.cardview.R.color.cardview_dark_background
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlin.math.PI
import kotlin.math.sin

class Radial_LinealActivity : AppCompatActivity() {

    private val tramos = mutableListOf<Tramo>()
    private lateinit var listView: ListView
    private lateinit var tramoAdapter: TramoAdapter
    private lateinit var behaviour: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_radial_lineal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MenuRadial::class.java)
            startActivity(intent)
        }

        // Permite controlar el estado del BottomSheet
        val bottomSheetTotal = findViewById<LinearLayout>(R.id.bottomSheet)
        behaviour = BottomSheetBehavior.from(bottomSheetTotal)

        listView = findViewById(R.id.lista)
        tramoAdapter = TramoAdapter(this, tramos)
        listView.adapter = tramoAdapter

        // Prueba de
        val grafica = findViewById<LineChart>(R.id.graficaPruebas)

        findViewById<Button>(R.id.generar).setOnClickListener {
            val intent = Intent(this,GraphicsActivity::class.java)
            // Poner extras cuando tengamos todas las ecuaciones
            startActivity(intent)
        }
        findViewById<TextView>(R.id.agregarTramos).setOnClickListener {
            tramos.add(0,Tramo())
            tramoAdapter.notifyDataSetChanged()
        }
    }

    fun suma() {
        if (360 == tramos.sumOf { it.ejeX.toIntOrNull() ?: 0 }
                .apply { findViewById<TextView>(R.id.total).text = this.toString() + " " }) {
            findViewById<Button>(R.id.generar).isEnabled = true
            findViewById<Button>(R.id.generar).setBackgroundColor(Color.parseColor("#7B1FA2"))
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            val entries = calcularSubida(5f, tramos[0].ejeX.toIntOrNull()?: 90)
            graficarSubida(entries)
        }
        else {
            findViewById<Button>(R.id.generar).isEnabled=true
            findViewById<Button>(R.id.generar).setBackgroundColor(ContextCompat.getColor(this, cardview_dark_background))
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
            val y = altura * ((x/beta) - ((1/(2* PI)) * sin(2*PI*(x/beta))))
            entries.add(Entry(x, y.toFloat()))
        }

        return entries
    }

    inner class TramoAdapter(context: Context, private val tramos: List<Tramo>) :
        ArrayAdapter<Tramo>(context, 0, tramos) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.tramo, parent, false)

            val tramo = tramos[position]

            val segmento = view.findViewById<AutoCompleteTextView>(R.id.segmentos)
            val ejeXText: TextView = view.findViewById(R.id.eje)
            val ecuacion = view.findViewById<AutoCompleteTextView>(R.id.ecuaciones)

            view.tag = position

            segmento.setAdapter(ArrayAdapter(context, R.layout.item_op, resources.getStringArray(R.array.segmentos)))
            ejeXText.text = tramo.ejeX
            ecuacion.setAdapter(ArrayAdapter(context, R.layout.item_op, resources.getStringArray(R.array.ecuaciones)))

            segmento.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val tramoIndex = view.tag as? Int ?: return@OnItemClickListener
                val selectedItem = parent.getItemAtPosition(position) as? String ?: return@OnItemClickListener
                tramos[tramoIndex].ecuacion = selectedItem
                when (selectedItem) {
                    "Det. Alto", "Det. Bajo" -> ecuacion.visibility = View.INVISIBLE
                    else -> ecuacion.visibility = View.VISIBLE
                }
            }

            ejeXText.doOnTextChanged { text, start, before, count ->
                val tramoIndex = view.tag as? Int ?: return@doOnTextChanged
                tramos[tramoIndex].ejeX = ejeXText.text.toString()
                suma()
            }

            ecuacion.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val tramoIndex = view.tag as? Int ?: return@OnItemClickListener
                val selectedItem = parent.getItemAtPosition(position) as? String ?: return@OnItemClickListener
                tramos[tramoIndex].ecuacion = selectedItem
            }

            return view
        }
    }



    inner class Tramo(
        var segmento: String = "Subida",
        var ejeX: String = "",
        var ecuacion: String = "Cicloidal"
    )
}