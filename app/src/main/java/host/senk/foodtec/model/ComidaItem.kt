package host.senk.foodtec.model

///AQUI VAN A SER RECOGIDOS LOS DATOS QUE NOS MANDE EL PHP

data class ComidaItem(
    val id: Int,
    val nombre: String,
    val precio: String, // Lo ponemos como String, es más fácil de manejar
    val imagen_url: String, // La URL de la foto en nuestro Hostinger
    val descripcion: String,
    val valoracion: String,
    val categoria: String
)
