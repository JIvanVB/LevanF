package svaj

class GeneradorCalculadoraSubida: GeneradorCalculadora {
    override fun crearCalculadoraCicloidal(): CalculadoraSVAJ {
        return CalculadoraSubidaCicloidal()
    }

    override fun crearCalculadoraMAS(): CalculadoraSVAJ {
        TODO("Not yet implemented")
    }

    override fun crearCalculadora4567(): CalculadoraSVAJ {
        TODO("Not yet implemented")
    }

    override fun crearCalculadora345(): CalculadoraSVAJ {
        TODO("Not yet implemented")
    }
}