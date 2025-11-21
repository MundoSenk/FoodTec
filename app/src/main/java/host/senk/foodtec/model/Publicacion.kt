package host.senk.foodtec.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Publicacion(
    val id_publicacion: Int,
    val titulo: String,
    val descripcion: String,
    val foto_url: String,
    val contacto_whatsapp: String,
    val tipo: String, // "Perdido" o "Encontrado"
    val fecha_publicacion: String,
    val nombre_usuario: String,
    val avatar_id: String,
    val usuario_id: String
) : Parcelable

// Y el sobre para la respuesta
data class PublicacionesResponse(
    val status: String,
    val publicaciones: List<Publicacion>?
)