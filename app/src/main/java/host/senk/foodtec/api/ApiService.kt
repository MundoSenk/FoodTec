package host.senk.foodtec.api

import host.senk.foodtec.model.LoginResponse
import host.senk.foodtec.model.RegistroResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    /**

     *
     * @FormUrlEncoded - Le dice a Retrofit que envíe los datos como un formulario web

     */
    @FormUrlEncoded
    @POST("registrarUsuario.php")
    fun registrarUsuario(
        // @Field("...") debe coincidir EXACTAMENTE con las claves de tu $_POST en PHP
        @Field("nombre") nombre: String,
        @Field("usuario") usuario: String,
        @Field("contra") contra: String,
        @Field("correo") correo: String
    ): Call<RegistroResponse> // Ahorita te explico qué es esto

    /**
     * Función para INICIAR SESIÓN.
     */
    @FormUrlEncoded
    @POST("loginUsuario.php")
    fun loginUsuario(
        @Field("usuario") usuario: String,
        @Field("contra") contra: String
    ): Call<LoginResponse> // Usará un NUEVO sobre de respuesta


}