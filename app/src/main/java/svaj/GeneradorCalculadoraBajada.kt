package svaj

/**
 * Fabrica para obtener calculadoras para los segmentos de bajada
 */
class GeneradorCalculadoraBajada: GeneradorCalculadora {

    /**
     * Regresa una calculadora de bajada con las ecuaciones cicloidales
     */
    override fun crearCalculadoraCicloidal(): CalculadoraSVAJ {
        return CalculadoraBajadaCicloidal()
    }

    /**
     * Regresa una calculadora de bajada con las ecuaciones MAS
     */
    override fun crearCalculadoraMAS(): CalculadoraSVAJ {
        TODO("Not yet implemented")
    }

    /**
     * Regresa una calculadora de bajada con las ecuaciones 4-5-6-7
     */
    override fun crearCalculadora4567(): CalculadoraSVAJ {
        TODO("Not yet implemented")
    }

    /**
     * Regresa una calculadora de bajada con las ecuaciones 3-4-5
     */
    override fun crearCalculadora345(): CalculadoraSVAJ {
        TODO("Not yet implemented")
    }
}