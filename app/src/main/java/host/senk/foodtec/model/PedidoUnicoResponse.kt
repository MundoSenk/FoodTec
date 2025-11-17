package host.senk.foodtec.model

/**
 * El "molde" para el PHP "francotirador" (obtenerMiPedidoActivo.php)
 * Espera un 'status' y UN solo objeto pedido.
 */
data class PedidoUnicoResponse(
    val status: String,
    val mensaje: String?,
    val pedido: Pedido? // ¡¡OJO: "pedido" (singular) y nulable!!
)