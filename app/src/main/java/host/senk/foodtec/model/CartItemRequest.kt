package host.senk.foodtec.model

// ¡El molde de CADA item que va en el JSON
// ¡OJO! No es 'Parcelable'
data class CartItemRequest(
    val id: Int,
    val cantidad: Int,
    val precio_unitario: Double, //
    val detalles_usuario: String
)