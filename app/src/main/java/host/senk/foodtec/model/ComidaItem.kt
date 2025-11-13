package host.senk.foodtec.model


import kotlinx.parcelize.Parcelize


import android.os.Parcelable


@Parcelize
data class ComidaItem(
    val id: Int,
    val nombre: String,
    val precio: String,
    val imagen_url: String,
    val descripcion: String,
    val valoracion: String,
    val categoria: String
) : Parcelable