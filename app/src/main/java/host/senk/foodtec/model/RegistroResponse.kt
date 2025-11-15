package host.senk.foodtec.model

// Esta es la "plantilla" para que Gson entienda la respuesta
// Los nombres de las variables (status, mensaje) DEBEN ser idénticos
// a las claves (keys) de tu JSON en PHP.
data class RegistroResponse(
    val status: String,
    val mensaje: String,
    val usuario: String?,
    val nombre: String?,
    val correo: String?
)