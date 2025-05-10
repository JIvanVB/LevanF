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
import svaj.CalculadoraDetenimientoAlto
import svaj.CalculadoraDetenimientoBajo
import svaj.CalculadoraSVAJ
import svaj.CalculadoraSubidaCicloidal
import svaj.GeneradorCalculadora
import svaj.GeneradorCalculadoraBajada
import svaj.GeneradorCalculadoraSubida

/**
 * GraphsActivity es la actividad que muestra gráficas de desplazamiento, velocidad, aceleración y sacudimiento
 * basadas en los datos de tramos proporcionados.
 */
class GraphsActivity : AppCompatActivity() {

    // Altura acumulada y altura inicial para cálculos
    var alturaAcumulada: Double = 0.0
    var alturaInicial = 0.0

    /**
     * Metodo llamado cuando se crea la actividad.
     * Configura la interfaz de usuario y los listeners de eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphics_results)

        // Configura la toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, Radial_LinealActivity::class.java)
            startActivity(intent)
        }

        // Obtiene los extras del Intent
        val extras = intent.extras

        // Configura los gráficos
        val desplazamientoChart = findViewById<LineChart>(R.id.Desplazamiento)
        val velocidadChart = findViewById<LineChart>(R.id.Velocidad)
        val aceleracionChart = findViewById<LineChart>(R.id.Aceleracion)
        val sacudimientoChart = findViewById<LineChart>(R.id.Golpeteo)

        // Configura los gráficos de línea
        setupLineChart(desplazamientoChart, "Desplazamiento")
        setupLineChart(velocidadChart, "Velocidad")
        setupLineChart(aceleracionChart, "Aceleración")
        setupLineChart(sacudimientoChart, "Sacudimiento")

        // Procesa los extras si no son nulos
        if (extras != null) {
            val tramos: ArrayList<Radial_LinealActivity.Tramo> =
                extras.get("tramos") as ArrayList<Radial_LinealActivity.Tramo>
            val rpm = extras.get("rpm") as Double

            // Lista de entradas para cada gráfico
            val entriesDesplazamiento = ArrayList<Entry>()
            val entriesVelocidad = ArrayList<Entry>()
            val entriesAceleracion = ArrayList<Entry>()
            val entriesSacudimiento = ArrayList<Entry>()

            // Calcula y carga los datos para cada gráfico
            var xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesDesplazamiento.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "desplazamiento", xAnterior)
                )
                loadChartData(desplazamientoChart, entriesDesplazamiento, "Desplazamiento")
                xAnterior += tramo.ejeX.toInt()
                alturaInicial = alturaAcumulada
            }

            xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesVelocidad.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "velocidad", xAnterior)
                )
                loadChartData(velocidadChart, entriesVelocidad, "Velocidad")
                xAnterior += tramo.ejeX.toInt()
            }

            xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesAceleracion.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "aceleracion", xAnterior)
                )
                loadChartData(aceleracionChart, entriesAceleracion, "Aceleración")
                xAnterior += tramo.ejeX.toInt()
            }

            xAnterior = 0
            for (tramo in tramos) {
                val calculadora = obtenerCalculadora(tramo)
                entriesSacudimiento.addAll(
                    calcularDatosGrafica(tramo, rpm, calculadora, "sacudimiento", xAnterior)
                )
                loadChartData(sacudimientoChart, entriesSacudimiento, "Sacudimiento")
                xAnterior += tramo.ejeX.toInt()
            }
        }
    }

    /**
     * Calcula los datos a graficar de un tramo dado y regresa una lista con los puntos a graficar.
     * @param tramo El tramo para el cual se calcularán los datos.
     * @param rpm Las revoluciones por minuto.
     * @param calculadora La calculadora a utilizar para los cálculos.
     * @param tipoGrafica El tipo de gráfico a calcular.
     * @param valorInicial El valor inicial para el cálculo.
     * @return Lista de entradas para la gráfica.
     */
    fun calcularDatosGrafica(
        tramo: Radial_LinealActivity.Tramo, rpm: Double,
        calculadora: CalculadoraSVAJ, tipoGrafica: String, valorInicial: Int
    ): ArrayList<Entry> {
        val teta = ArrayList<Double>()
        val beta = tramo.ejeX.toInt()
        val altura = tramo.altura.toDoubleOrNull() ?: 0.0
        val segmento = tramo.segmento.lowercase()
        val entries = ArrayList<Entry>()
        val w = rpm.aRadianesSegundos()

        alturaAcumulada += if (segmento == "subida") altura else -altura

        for (x in valorInicial..beta + valorInicial) {
            teta.add(x.aRadianes())
        }

        val betaRadianes = beta.aRadianes()
        for (x in teta) {
            // Realiza el cálculo en base al tipo de gráfico
            val y = when (tipoGrafica) {
                "desplazamiento" -> {
                    calculadora.calcularDesplazamiento(
                        x - valorInicial.aRadianes(),
                        altura,
                        betaRadianes,
                        alturaInicial
                    )
                }
                "velocidad" -> {
                    calculadora.calcularVelocidad(
                        x - valorInicial.aRadianes(),
                        altura,
                        betaRadianes,
                        w
                    )
                }
                "aceleracion" -> {
                    calculadora.calcularAceleracion(
                        x - valorInicial.aRadianes(),
                        altura,
                        betaRadianes,
                        w
                    )
                }
                "sacudimiento" -> {
                    calculadora.calcularSacudimiento(
                        x - valorInicial.aRadianes(),
                        altura,
                        betaRadianes,
                        w
                    )
                }
                else -> 0.0
            }

            entries.add(Entry(x.toFloat(), y.toFloat()))
        }

        return entries
    }

    /**
     * Convierte un valor de rpm a radianes / segundo.
     */
    fun Double.aRadianesSegundos(): Double {
        return this * 2 * Math.PI / 60
    }

    /**
     * Convierte un valor en grados a radianes.
     */
    fun Int.aRadianes(): Double {
        return this * Math.PI / 180
    }

    /**
     * Recibe un objeto de tramo y determina qué tipo de calculadora requiere para generar la gráfica
     * en base al segmento y el tipo de ecuación del tramo.
     * @param tramo El tramo para el cual se determinará la calculadora.
     * @return La calculadora adecuada para el tramo.
     */
    private fun obtenerCalculadora(tramo: Radial_LinealActivity.Tramo): CalculadoraSVAJ {
        val segmento = tramo.segmento.lowercase()
        val ecuacion = tramo.ecuacion.lowercase()
        var calculadora: CalculadoraSVAJ = CalculadoraSubidaCicloidal()
        val generador: GeneradorCalculadora = if (segmento == "subida") GeneradorCalculadoraSubida()
        else GeneradorCalculadoraBajada()

        when (segmento) {
            "subida", "bajada" -> {
                if (ecuacion == "cicloidal") {
                    calculadora = generador.crearCalculadoraCicloidal()
                }
            }
            "det. alto" -> {
                calculadora = CalculadoraDetenimientoAlto()
            }
            else -> {
                calculadora = CalculadoraDetenimientoBajo()
            }
        }

        return calculadora
    }

    /**
     * Define parámetros de configuración para desplegar las gráficas.
     * @param lineChart El gráfico de línea a configurar.
     * @param descText La descripción del gráfico.
     */
    private fun setupLineChart(lineChart: LineChart, descText: String) {
        // Habilitar interacciones
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        // Configuración de los ejes
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawGridLines(true)

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

    /**
     * Recibe los datos a insertar en la gráfica y la dibuja.
     * @param lineChart El gráfico de línea en el que se cargarán los datos.
     * @param entries Los datos a insertar en la gráfica.
     * @param nombreGrafica El nombre de la gráfica.
     */
    private fun loadChartData(lineChart: LineChart, entries: ArrayList<Entry>, nombreGrafica: String) {
        val dataSet = LineDataSet(entries, nombreGrafica).apply {
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
