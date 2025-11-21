package host.senk.foodtec.model

data class Notificacion(
    val id_notificacion: Int,
    val titulo: String,
    val mensaje: String,
    val tipo: String, // "objeto", "pedido"
    val id_referencia: Int,
    val fecha_creacion: String
)

data class NotificacionesResponse(
    val status: String,
    val notificaciones: List<Notificacion>?
)