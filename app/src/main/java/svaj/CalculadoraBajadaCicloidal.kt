package svaj

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class CalculadoraBajadaCicloidal: CalculadoraSVAJ {
    override fun calcularDesplazamiento(x: Double, altura: Double, beta: Double): Double {
        return altura - (altura * ((x / beta) - ((1 / (2 * PI)) * sin(2 * PI * (x / beta)))))
    }

    override fun calcularVelocidad(x: Double, altura: Double, beta: Double, w: Double): Double {
        return -((altura / beta) * (1 - cos(2 * PI * (x / beta)))) * w
    }

    override fun calcularAceleracion(x: Double, altura: Double, beta: Double, w: Double): Double {
        return -2 * PI * (altura / beta.pow(2)) * sin(2 * PI * (x / beta)) * w.pow(2)
    }

    override fun calcularSacudimiento(x: Double, altura: Double, beta: Double, w: Double): Double {
        return -4 * PI.pow(2) * (altura / beta.pow(3)) * cos(2 * PI * (x / beta)) * w.pow(3)
    }
}