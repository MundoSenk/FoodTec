package host.senk.foodtec.model

// El "sobre" que nos manda 'obtenerMisPedidos.php'
data class PedidosResponse(
    val status: String,
    val mensaje: String?, // ¡Nulable! (con '?')
    val pedidos: List<Pedido>? // ¡Una lista de los moldes de arriba!
)
