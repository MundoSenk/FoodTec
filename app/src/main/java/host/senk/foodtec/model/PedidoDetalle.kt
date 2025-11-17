package host.senk.foodtec.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable


@Parcelize
data class PedidoDetalle(
    val nombre: String?,
    val cantidad: Int?,
    val precio_unitario: String?,
    val imagen_url: String?, //
    val detalles_usuario: String?
) : Parcelable