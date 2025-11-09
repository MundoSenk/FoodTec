package host.senk.foodtec.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // --- ¡AQUÍ VA TU URL! ---
    // Esta es la "oficina central" de tu API.
    // Nota que termina con un "/"
    private const val BASE_URL = "https://senk.host/api/"

    // 1. Creamos el constructor de Retrofit (el cartero)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Le damos su ruta base
            .addConverterFactory(GsonConverterFactory.create()) // Le damos su "traductor" (Gson)
            .build()
    }

    // 2. Creamos la "instancia" del menú de órdenes
    // Esto es lo que usaremos para llamar a las funciones
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}