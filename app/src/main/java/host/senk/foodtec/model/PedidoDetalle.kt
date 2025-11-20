package host.senk.foodtec.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName // <-- ¡¡IMPORT NUEVO!!

@Parcelize
data class PedidoDetalle(

    @SerializedName("nombre_alimento") // <-- Le dice a Gson cómo se llama en el PHP
    val nombre: String?,

    val cantidad: Int?,
    val precio_unitario: String?,

    //Y AQUÍ TAMBIÉN
    @SerializedName("imagen_url") //
    val imagen_url: String?,

    val detalles_usuario: String?,

    val alimento_id: Int?

) : Parcelable