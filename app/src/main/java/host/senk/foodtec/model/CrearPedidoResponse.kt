package host.senk.foodtec.model

// ¡El molde pa la respuesta del crearPedido.php
data class CrearPedidoResponse(
    val status: String,
    val mensaje: String,
    val id_pedido: Int?
)