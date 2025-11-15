package host.senk.foodtec.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

// El "renglón" de nuestro carrito
// También es Parcelable pa' poder mandarlo entre pantallas
@Parcelize
data class CartItem(
    val id: Int,
    val nombre: String,
    val precio_unitario: String,
    val imagen_url: String,


    var cantidad: Int = 1, // 'var' pa' poder cambiarla (si pide 2)
    val detalles_usuario: String // El sin salsa
) : Parcelable