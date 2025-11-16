package host.senk.foodtec.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

// El "molde" pa' un solo pedido (comanda)!
// a los que pusimos en el 'SELECT' del PHP!
@Parcelize
data class Pedido(
    val id_pedido: Int,
    val lugar_entrega: String,
    val costo_final: String, // ¡Lo jalamos como String, es más fácil!
    val metodo_pago: String,
    val estatus: String,
    val fecha_pedido: String
) : Parcelable
