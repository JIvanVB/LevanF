package com.rafd.levanf

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import svaj.CalculadoraBajadaCicloidal
import svaj.CalculadoraSVAJ
import svaj.CalculadoraSubidaCicloidal

class GraphsActivity : AppCompatActivity() {
    var rpm: Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphics_results)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, Radial_LinealActivity::class.java)
            startActivity(intent)
        }

        val extras = intent.extras

        val desplazamientoChart = findViewById<LineChart>(R.id.Desplazamiento)
        val velocidadChart = findViewById<LineChart>(R.id.Velocidad)
        val aceleracionChart = findViewById<LineChart>(R.id.Aceleracion)
        val sacudimientoChart = findViewById<LineChart>(R.id.Golpeteo)

        setupLineChart(desplazamientoChart, "Desplazamiento")
        setupLineChart(velocidadChart, "Velocidad")
        setupLineChart(aceleracionChart, "Aceleración")
        setupLineChart(sacudimientoChart, "Sacudimiento")

        val tramos: ArrayList<Radial_LinealActivity.Tramo>
        if (extras != null) {
            tramos = extras.get("tramos") as ArrayList<Radial_LinealActivity.Tramo>
            rpm = extras.get("rpm") as Double

            val entriesDesplazamiento = ArrayList<Entry>()
            val entriesVelocidad = ArrayList<Entry>()
            val entriesAceleracion = ArrayList<Entry>()
            val entriesSacudimiento = ArrayList<Entry>()

            var xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesDesplazamiento.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "desplazamiento", xAnterior)
                )
                loadChartData(desplazamientoChart, entriesDesplazamiento)
                xAnterior += tramo.ejeX.toInt()
            }

            xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesVelocidad.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "velocidad", xAnterior)
                )
                loadChartData(velocidadChart, entriesVelocidad)
                xAnterior += tramo.ejeX.toInt()
            }

            xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesAceleracion.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "aceleracion", xAnterior)
                )
                loadChartData(aceleracionChart, entriesAceleracion)
                xAnterior += tramo.ejeX.toInt()
            }

            xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesSacudimiento.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "sacudimiento", xAnterior)
                )
                loadChartData(sacudimientoChart, entriesSacudimiento)
                xAnterior += tramo.ejeX.toInt()
            }
        }
    }

    fun calcularDatosGrafica(tramo: Radial_LinealActivity.Tramo, rpm: Double,
                             calculadora: CalculadoraSVAJ, tipoGrafica:String, valorInicial: Int): ArrayList<Entry> {
        val teta = ArrayList<Double>()
        val beta = tramo.ejeX.toInt()
        val altura = tramo.altura.toDouble()
        val entries = ArrayList<Entry>()
        val w = rpm.aRadianesSegundos();

        // change to start on last beta
        for (x in valorInicial..beta + valorInicial) {
            teta.add(x.aRadianes())
        }

        val betaRadianes = beta.aRadianes();
        for (x in teta) {
            val y = when (tipoGrafica) {
                "desplazamiento" -> { calculadora.calcularDesplazamiento(x-valorInicial.aRadianes(), altura, betaRadianes) }
                "velocidad" -> { calculadora.calcularVelocidad(x-valorInicial.aRadianes(), altura, betaRadianes, w) }
                "aceleracion" -> { calculadora.calcularAceleracion(x-valorInicial.aRadianes(), altura, betaRadianes, w) }
                "sacudimiento" -> { calculadora.calcularSacudimiento(x-valorInicial.aRadianes(), altura, betaRadianes, w) }
                else -> 0.0
            }

            entries.add(Entry(x.toFloat(), y.toFloat()))
        }

        return entries
    }

    fun Double.aRadianesSegundos(): Double {
        return this * 2 * Math.PI / 60
    }

    fun Int.aRadianes(): Double {
        return this * Math.PI / 180
    }

    private fun obtenerCalculadora(tramo: Radial_LinealActivity.Tramo): CalculadoraSVAJ {
        val segmento = tramo.segmento.lowercase()
        val ecuacion = tramo.ecuacion.lowercase()
        var calculadora: CalculadoraSVAJ = CalculadoraSubidaCicloidal()
        when (segmento) {
            "subida" -> {
                if (ecuacion == "cicloidal") { calculadora = CalculadoraSubidaCicloidal() }
            }
            "bajada" -> {
                if (ecuacion == "cicloidal") {
                    calculadora = CalculadoraBajadaCicloidal()
                }
            }
            "detAlto" -> {
                //
            }
            else -> {

            }
        }

        return calculadora
    }

    private fun setupLineChart(lineChart: LineChart, descText : String) {
        // Habilitar interacciones
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

//        // Configuración de los ejes
//        val xAxis = lineChart.xAxis
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.setDrawGridLines(true)
//
//        val yAxisLeft = lineChart.axisLeft
//        yAxisLeft.setDrawGridLines(true)
//        yAxisLeft.axisMinimum = 0f
//
//        val yAxisRight = lineChart.axisRight
//        yAxisRight.isEnabled = false
//
//        // Configuración de la leyenda
//        val legend = lineChart.legend
//        legend.form = Legend.LegendForm.LINE

        lineChart.post {
            val description = Description()
            description.text = descText
            description.textSize = 14f
            description.textColor = Color.DKGRAY
            description.isEnabled = true

            // Medir el ancho del texto
            val paint = Paint()
            paint.textSize = description.textSize
            val textWidth = paint.measureText(description.text)

            // Ajustar la posición correctamente
            val centerX = lineChart.width / 2.0f + textWidth / 2f
            val positionY = 40f // Altura de la descripción

            description.setPosition(centerX, positionY)

            // Margen superior extra para evitar que se superponga
            lineChart.extraTopOffset = 40f

            lineChart.description = description
            lineChart.invalidate() // Redibujar
        }

    }


    private fun loadChartData(lineChart: LineChart, entries: ArrayList<Entry>) {
        val dataSet = LineDataSet(entries, "Desplazamiento s (mm)").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 3f
            setDrawCircles(false)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }
}

