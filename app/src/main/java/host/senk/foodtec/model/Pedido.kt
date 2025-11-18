package host.senk.foodtec.model

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Pedido(

    @SerializedName("id_pedido")
    val id_pedido: Int?,

    @SerializedName("lugar_entrega")
    val lugar_entrega: String?,

    @SerializedName("costo_final")
    val costo_final: String?, // <-- ¡El que nos duele!

    @SerializedName("metodo_pago")
    val metodo_pago: String?,

    @SerializedName("estatus")
    val estatus: String?,

    @SerializedName("fecha_pedido")
    val fecha_pedido: String?,

    @SerializedName("detalles")
    val detalles: List<PedidoDetalle?>?,

    @SerializedName("nombre_cliente")
    val nombre_cliente: String?,


    @SerializedName("valoracion_cliente")
    val valoracion_cliente: String?

) : Parcelable