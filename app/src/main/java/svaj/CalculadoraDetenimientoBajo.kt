package svaj

class CalculadoraDetenimientoBajo: CalculadoraSVAJ {
    override fun calcularDesplazamiento(x: Double, altura: Double, beta: Double, alturaInicial: Double): Double {
        return 0.0
    }

    override fun calcularVelocidad(x: Double, altura: Double, beta: Double, w: Double): Double {
        return 0.0
    }

    override fun calcularAceleracion(x: Double, altura: Double, beta: Double, w: Double): Double {
        return 0.0
    }

    override fun calcularSacudimiento(x: Double, altura: Double, beta: Double, w: Double): Double {
        return 0.0
    }
}