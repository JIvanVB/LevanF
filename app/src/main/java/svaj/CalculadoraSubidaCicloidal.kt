package svaj

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Calculadora que implementa las funciones para graficar el movimiento de subida cicloidal de una leva
 */
class CalculadoraSubidaCicloidal: CalculadoraSVAJ {
    override fun calcularDesplazamiento(x: Double, altura: Double, beta: Double, alturaInicial: Double): Double {
        return altura * ((x / beta) - ((1 / (2 * PI)) * sin(2 * PI * (x / beta)))) + alturaInicial
    }

    override fun calcularVelocidad(x: Double, altura: Double, beta: Double, w: Double): Double {
        return ((altura / beta) * (1 - cos(2 * PI * (x / beta)))) * w
    }

    override fun calcularAceleracion(x: Double, altura: Double, beta: Double, w: Double): Double {
        return 2 * PI * (altura / beta.pow(2)) * sin(2 * PI * (x / beta)) * w.pow(2)
    }

    override fun calcularSacudimiento(x: Double, altura: Double, beta: Double, w: Double): Double {
        return 4 * PI.pow(2) * (altura / beta.pow(3)) * cos(2 * PI * (x / beta)) * w.pow(3)
    }
}