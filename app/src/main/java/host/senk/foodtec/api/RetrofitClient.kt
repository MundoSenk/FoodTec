package host.senk.foodtec.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient // <-- ¡¡IMPORT NUEVO!!
import okhttp3.logging.HttpLoggingInterceptor // <-- ¡¡IMPORT NUEVO!!
import java.util.concurrent.TimeUnit // <-- ¡IMPORT NUEVO!

object RetrofitClient {

    private const val BASE_URL = "https://senk.host/api/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()



    // Creamos el chismoso
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Le decimos que chismee TODO el "cuerpo"
    }

    // Creamos un motor"OkHttpClient que USE el chismoso
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // ¡Lo conectamos!
        .connectTimeout(30, TimeUnit.SECONDS) // }
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    //Tuneamos Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client) // <-- ¡¡LE DECIMOS QUE USE EL "MOTOR TUNEADO"!!
            .build()
    }


    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}