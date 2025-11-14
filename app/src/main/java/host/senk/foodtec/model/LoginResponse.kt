package host.senk.foodtec.model

// Plantilla para la respuesta de login.php
// ¡OJO! Hacemos 'nombre' y 'usuario' NULABLES (con ?)
// porque si el login falla (ej. pass incorrecto),
// el PHP no nos va a mandar esos campos, y si no son nulables,
// Gson crashearía.
data class LoginResponse(
    val status: String,
    val mensaje: String,
    val nombre: String?,
    val usuario: String?,
    val correo: String?
)