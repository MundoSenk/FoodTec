package host.senk.foodtec

// Imports de Retrofit que le dicen cómo "anotar" las llamadas
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    /**
     * Esta función llamará a nuestro archivo 'registrarUsuario.php'
     *
     * @FormUrlEncoded - Le dice a Retrofit que envíe los datos como un formulario web
     * (justo como lo espera nuestro $_POST['...'] en PHP).
     *
     * @POST("registrarUsuario.php") - Le dice que use el método POST y a qué
     * archivo específico llamar (dentro de nuestra URL base).
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