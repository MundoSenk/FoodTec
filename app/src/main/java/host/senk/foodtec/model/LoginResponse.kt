package host.senk.foodtec.model


data class LoginResponse(
    val status: String,
    val mensaje: String,
    val nombre: String?,
    val usuario: String?,
    val correo: String?,
    val es_foodter: Boolean?
)