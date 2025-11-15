package host.senk.foodtec.model

// Este molde es el papa de los model CartItemRequest y CrearPedidoRequest- Este es el JSON QUE DEBE LLEGAR A LA BD
data class PedidoRequest(
    val usuario_id: String,
    val lugar_entrega: String,
    val costo_final: Double, // ¡'Double' pa' que jale!
    val metodo_pago: String,
    val items: List<CartItemRequest> // ¡Una lista de los "renglones" de arriba!
)