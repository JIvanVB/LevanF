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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class GraphicsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphics_results)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, Radial_LinealActivity::class.java)
            startActivity(intent)
        }

        val desplazamientoChart = findViewById<LineChart>(R.id.Desplazamiento)
        val velocidadChart = findViewById<LineChart>(R.id.Velocidad)
        val aceleracionChart = findViewById<LineChart>(R.id.Aceleracion)
        val golpeteoChart = findViewById<LineChart>(R.id.Golpeteo)

        setupLineChart(desplazamientoChart, "Desplazamiento, s (mm) vs. Posición angular, β (°)")
        loadChartData(desplazamientoChart)

        setupLineChart(velocidadChart, "Velocidad, m/s vs. Posición angular, β (°)")
        loadChartData(velocidadChart)

        setupLineChart(aceleracionChart, "Aceleración, m/s² vs. Posición angular, β (°)")
        loadChartData(aceleracionChart)

        setupLineChart(golpeteoChart, "Golpeteo, ° vs. Posición angular, β (°)")
        loadChartData(golpeteoChart)
    }

    private fun setupLineChart(lineChart: LineChart, descText : String) {
        // Habilitar interacciones
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        // Configuración de los ejes
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.axisMinimum = 0f

        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false

        // Configuración de la leyenda
        val legend = lineChart.legend
        legend.form = Legend.LegendForm.LINE

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


    private fun loadChartData(lineChart: LineChart) {
        val entries = ArrayList<Entry>()

        // Datos aproximados para recrear la curva
        val datos = mapOf(
            0f to 0f, 30f to 5f, 60f to 25f, 90f to 38f, 120f to 40f,
            150f to 40f, 180f to 40f, 210f to 38f, 240f to 25f, 270f to 5f, 300f to 0f
        )

        for ((x, y) in datos) {
            entries.add(Entry(x, y))
        }

        // Crear dataset
        val dataSet = LineDataSet(entries, "Desplazamiento s (mm)").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 3f
            setDrawCircles(false)
            setDrawValues(false)
        }

        // Agregar punto máximo
        val maxEntry = Entry(120f, 40f) // Punto máximo
        val maxDataSet = LineDataSet(listOf(maxEntry), "MAX: 40,00").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
            setDrawCircles(true)
            setCircleColor(Color.RED)
            setDrawValues(true)
        }

        // Agregar datasets y actualizar la gráfica
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)
        dataSets.add(maxDataSet)

        val lineData = LineData(dataSets)
        lineChart.data = lineData
        lineChart.invalidate() // Refrescar gráfico
    }
}

