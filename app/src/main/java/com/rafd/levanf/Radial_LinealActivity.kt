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
import android.widget.Spinner
import androidx.cardview.R.color.cardview_dark_background
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged

class Radial_LinealActivity : AppCompatActivity() {

    private val tramos = mutableListOf<Tramo>()
    private lateinit var listView: ListView
    private lateinit var tramoAdapter: TramoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_radial_lineal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listView = findViewById(R.id.lista)
        tramoAdapter = TramoAdapter(this, tramos)
        listView.adapter = tramoAdapter


        findViewById<Button>(R.id.generar).setOnClickListener {startActivity(Intent(this,GraphicsResults::class.java))}
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
        }
        else{
            findViewById<Button>(R.id.generar).isEnabled=true
            findViewById<Button>(R.id.generar).setBackgroundColor(ContextCompat.getColor(this, cardview_dark_background))}
    }

    inner class TramoAdapter(context: Context, private val tramos: List<Tramo>) :
        ArrayAdapter<Tramo>(context, 0, tramos) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.tramo, parent, false)

            val tramo = tramos[position]

            val segmento: Spinner = view.findViewById(R.id.segmentos)
            val ejeXText: TextView = view.findViewById(R.id.eje)
            val ecuacion: Spinner = view.findViewById(R.id.ecuaciones)


            view.tag = position

            val segmentoAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                context.resources.getStringArray(R.array.segmentos)
            )
            segmentoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            segmento.adapter = segmentoAdapter
            val segmentoPosition = segmentoAdapter.getPosition(tramo.segmento)
            segmento.setSelection(segmentoPosition)

            ejeXText.text = tramo.ejeX

            val ecuacionAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                context.resources.getStringArray(R.array.ecuaciones)
            )
            ecuacionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            ecuacion.adapter = ecuacionAdapter
            val ecuacionPosition = ecuacionAdapter.getPosition(tramo.ecuacion)
            ecuacion.setSelection(ecuacionPosition)

            segmento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    when (segmento.selectedItem.toString()) {
                        "Det. Alto", "Det. Bajo" -> ecuacion.visibility = View.GONE
                        else -> ecuacion.visibility = View.VISIBLE
                    }
                    val tramoIndex = view?.tag as? Int ?: return
                    tramos[tramoIndex].segmento = segmento.selectedItem.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            ejeXText.doOnTextChanged { text, start, before, count ->
                val tramoIndex = view.tag as? Int ?: return@doOnTextChanged
                tramos[tramoIndex].ejeX = ejeXText.text.toString()
                suma()
            }

            ecuacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val tramoIndex = view?.tag as? Int ?: return
                    tramos[tramoIndex].ecuacion = ecuacion.selectedItem.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
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