package host.senk.foodtec.api

import host.senk.foodtec.model.AvatarRequest
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.PedidoRequest
import host.senk.foodtec.model.LoginResponse
import host.senk.foodtec.model.MenuResponse
import host.senk.foodtec.model.RegistroResponse
import host.senk.foodtec.model.PedidoUnicoResponse
import host.senk.foodtec.model.PedidosResponse
import host.senk.foodtec.model.PublicacionesResponse

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET

interface ApiService {

    //  REGISTRO
    @FormUrlEncoded
    @POST("registrarUsuario.php")
    fun registrarUsuario(
        @Field("nombre") nombre: String,
        @Field("usuario") usuario: String,
        @Field("contra") contra: String,
        @Field("correo") correo: String
    ): Call<RegistroResponse>


    // LOGIN
    @FormUrlEncoded
    @POST("loginUsuario.php")
    fun loginUsuario(
        @Field("usuario") usuario: String,
        @Field("contra") contra: String
    ): Call<LoginResponse>


    // OBTENER MENU

    @FormUrlEncoded
    @POST("obtenerMenu.php")
    fun obtenerMenu(
        @Field("categoria") categoria: String
    ): Call<MenuResponse>


    // VERIFICAR CÓDIGO DE REGISTRO
    @FormUrlEncoded
    @POST("verify.php")
    fun verificarCodigo(
        @Field("correo") correo: String,
        @Field("codigo") codigo: String
    ): Call<RegistroResponse>


    //  RECUPERAR CONTRASEÑA (ENVÍA EMAIL)
    @FormUrlEncoded
    @POST("forgot_password.php")
    fun forgotPassword(
        @Field("email") email: String
    ): Call<LoginResponse>

    //  RESETEAR CONTRASEÑA TOKEN + NUEVA CONTRA
    @FormUrlEncoded
    @POST("reset_password_api.php")
    fun resetPassword(
        @Field("token") token: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    //  CREAR PEDIDO (USA JSON EN @Body)

    @POST("crearPedido.php")
    fun crearPedido(
        @Body pedido: PedidoRequest
    ): Call<CrearPedidoResponse>

    @FormUrlEncoded
    @POST("obtenerMisPedidos.php")
    fun obtenerMisPedidos(
        @Field("usuario_id") usuarioId: String // Le mandamos el "RENO"
    ): Call<PedidosResponse> // Y esperamos el "sobre" nuevo!


    @FormUrlEncoded
    @POST("buscarMenu.php") //
    fun buscarMenu(
        @Field("termino_busqueda") termino: String
    ): Call<MenuResponse> // REUTILIZAMOS EL 'MenuResponse'!!


    @FormUrlEncoded
    @POST("habilitarFoodter.php")
    fun habilitarFoodter(
        @Field("usuario_id") usuarioId: String,
        @Field("amabilidad") amabilidad: Int,
        @Field("interactuar") interactuar: Int,
        @Field("acepta_propinas") aceptaPropinas: Int, // 1 para SÍ, 0 para NO
        @Field("porque_texto") porqueTexto: String
    ): Call<CrearPedidoResponse>

    @FormUrlEncoded
    @POST("obtenerPedidosDisponibles.php")
    fun obtenerPedidosDisponibles(
        @Field("foodter_id") foodterId: String //
    ): Call<PedidosResponse>


    @FormUrlEncoded
    @POST("aceptarPedido.php")
    fun aceptarPedido(
        @Field("foodter_id") foodterId: String,
        @Field("pedido_id") pedidoId: Int
    ): Call<CrearPedidoResponse>


    @FormUrlEncoded
    @POST("actualizarEstatusPedido.php")
    fun actualizarEstatusPedido(
        @Field("foodter_id") foodterId: String,
        @Field("pedido_id") pedidoId: Int,
        @Field("nuevo_estatus") nuevoEstatus: String // "En camino" o "Entregado"
    ): Call<CrearPedidoResponse>


    @FormUrlEncoded
    @POST("actualizarStatusFoodter.php")
    fun actualizarStatusFoodter(
        @Field("foodter_id") foodterId: String,
        @Field("nuevo_status") nuevoStatus: String // "Activo" o "Inactivo"
    ): Call<CrearPedidoResponse>


    @FormUrlEncoded
    @POST("obtenerMiPedidoActivo.php")
    fun obtenerMiPedidoActivo(
        @Field("foodter_id") foodterId: String
    ): Call<PedidoUnicoResponse>


    @POST("actualizarAvatar.php")
    fun actualizarAvatar(
        @Body request: AvatarRequest
    ): Call<CrearPedidoResponse>



    // CALIFICAR PEDIDO (Sirve para Cliente -> Foodter Y Foodter -> Cliente)
    @FormUrlEncoded
    @POST("calificar_pedido.php")
    fun calificarPedido(
        @Field("pedido_id") pedidoId: Int,
        @Field("usuario_id") usuarioId: String,
        @Field("calificacion") calificacion: Float,
        @Field("rol_calificador") rol: String,
        @Field("items_json") itemsJson: String? = null
    ): Call<CrearPedidoResponse>


    @FormUrlEncoded
    @POST("actualizarTelefono.php")
    fun actualizarTelefono(
        @Field("usuario_id") usuarioId: String,
        @Field("telefono") telefono: String
    ): Call<CrearPedidoResponse>


    // OBJETOS PERDIDOS
    @GET("obtenerPublicaciones.php")
    fun obtenerPublicaciones(): Call<PublicacionesResponse>


    // SUBIR PUBLICACIÓN (Multipart)
    @Multipart
    @POST("crearPublicacion.php")
    fun crearPublicacion(
        @Part("usuario_id") usuarioId: RequestBody,
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("contacto") contacto: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part imagen: MultipartBody.Part // <-- ¡El archivo!
    ): Call<CrearPedidoResponse>



}
