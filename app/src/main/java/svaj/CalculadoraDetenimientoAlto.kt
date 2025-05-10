package svaj

/**
 * Calculadora para graficar el detenimiento alto de una leva
 */
class CalculadoraDetenimientoAlto: CalculadoraSVAJ {
    override fun calcularDesplazamiento(x: Double, altura: Double, beta: Double, alturaInicial: Double): Double {
        return alturaInicial
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