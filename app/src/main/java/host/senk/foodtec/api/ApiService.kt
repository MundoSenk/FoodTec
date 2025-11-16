package host.senk.foodtec.api

import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.PedidoRequest
import host.senk.foodtec.model.LoginResponse
import host.senk.foodtec.model.MenuResponse
import host.senk.foodtec.model.RegistroResponse
import host.senk.foodtec.model.PedidosResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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

}
