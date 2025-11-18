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
    @SerializedName("imagen") // <-- Le dice a Gson cómo se llama en el PHP
    val imagen_url: String?,

    val detalles_usuario: String?

) : Parcelable