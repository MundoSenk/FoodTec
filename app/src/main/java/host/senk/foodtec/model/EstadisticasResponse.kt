package host.senk.foodtec.model

data class EstadisticasResponse(
    val status: String,
    val pedidos_como_cliente: Int,
    val pedidos_como_foodter: Int,
    val es_usuario_god: Boolean
)