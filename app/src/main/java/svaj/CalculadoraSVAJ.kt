package svaj

/**
 * Define los métodos para que una calculadora realice las operaciones para graficar el
 * desplazamiento, velocidad, aceleración, y el sacudimiento de una leva
 */
interface CalculadoraSVAJ {
    fun calcularDesplazamiento(x: Double, altura: Double, beta: Double, alturaInicial: Double): Double
    fun calcularVelocidad(x: Double, altura: Double, beta: Double, w: Double): Double
    fun calcularAceleracion(x: Double, altura: Double, beta: Double, w: Double): Double
    fun calcularSacudimiento(x: Double, altura: Double, beta: Double, w: Double): Double
}