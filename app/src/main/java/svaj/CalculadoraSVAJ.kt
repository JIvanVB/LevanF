package svaj

interface CalculadoraSVAJ {
    fun calcularDesplazamiento(x: Double, altura: Double, beta: Double, alturaInicial: Double): Double
    fun calcularVelocidad(x: Double, altura: Double, beta: Double, w: Double): Double
    fun calcularAceleracion(x: Double, altura: Double, beta: Double, w: Double): Double
    fun calcularSacudimiento(x: Double, altura: Double, beta: Double, w: Double): Double
}