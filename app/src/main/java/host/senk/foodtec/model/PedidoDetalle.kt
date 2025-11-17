package host.senk.foodtec.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

/**
 * El "sub-molde" para el JSON "mamalón".
 * Este representa un solo renglón (ej. "2x Hamburguesa", "1x Tacos")
 */
@Parcelize
data class PedidoDetalle(
    val nombre_alimento: String?,
    val cantidad: Int?,
    val precio_unitario: String?,
    val imagen: String?,
    val detalles_usuario: String? // Los "sin salsa"
) : Parcelable