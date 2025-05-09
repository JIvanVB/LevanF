package svaj

/**
 * Define los métodos para que un generador de calculadoras de un tipo de segmento particular
 * provea calculadoras para las distintas ecuaciones con las que se puede calcular
 *
 * Implementa el patrón de fábrica abstracta
 */
interface GeneradorCalculadora {
    fun crearCalculadoraCicloidal(): CalculadoraSVAJ
    fun crearCalculadoraMAS(): CalculadoraSVAJ
    fun crearCalculadora4567(): CalculadoraSVAJ
    fun crearCalculadora345(): CalculadoraSVAJ
}