package host.senk.foodtec.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast // ¡Import!
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import host.senk.foodtec.R
import android.util.Log // ¡Import pa' ver errores!
import android.content.Intent

import androidx.recyclerview.widget.LinearLayoutManager //  Layout
import androidx.recyclerview.widget.RecyclerView //  pa RecyclerView
import host.senk.foodtec.adapter.MenuAdapter
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.model.MenuResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import host.senk.foodtec.model.ComidaItem


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ///SALUDO
        val tvBienvenido: TextView = findViewById(R.id.tvBienvenido)
        val nombreUsuario = intent.getStringExtra("NOMBRE_USUARIO") ?: "Usuario"
        tvBienvenido.text = "Bienvenido, $nombreUsuario"

        //AMARRAMOS LOS RECYCLERV
        val rvComida: RecyclerView = findViewById(R.id.rvComida)
        val rvBebidas: RecyclerView = findViewById(R.id.rvBebidas)

        // Les decimos que se pongan en horizontal
        rvComida.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvBebidas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        // Llamamos a nuestra nueva función de cartero pa las dos listas!
        cargarMenu("Comida", rvComida)
        cargarMenu("Bebida", rvBebidas)

    }


    //  JALAR EL MENY
    private fun cargarMenu(categoria: String, recyclerView: RecyclerView) {

        // Llamamos al cartero
        val call = RetrofitClient.apiService.obtenerMenu(categoria)


        call.enqueue(object : Callback<MenuResponse> {

            //  SI EL CARTERO LLEGÓ
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val menuRespuesta = response.body()!!


                    // Leemos PHP
                    if (menuRespuesta.status == "exito") {


                        // Checamos si el menuSÍ vino y no es nulo
                        menuRespuesta.menu?.let { listaDeMenu ->



                            val listenerDelClick = { comidaItem: ComidaItem ->


                                val intent = Intent(this@HomeActivity, DetailsActivity::class.java)

                                // LE METEMOS EL PLATILLO
                                intent.putExtra("COMIDA_SELECCIONADA", comidaItem)
                                startActivity(intent)
                            }


                            val adapter = MenuAdapter(listaDeMenu, listenerDelClick)
                            recyclerView.adapter = adapter

                        } ?: run {

                            Toast.makeText(this@HomeActivity, "Error: El PHP dijo 'exito' pero no mandó menú", Toast.LENGTH_LONG).show()
                        }

                    } else {


                        val errorMsg = menuRespuesta.mensaje ?: "Error desconocido del PHP"
                        Toast.makeText(this@HomeActivity, "Error del PHP: $errorMsg", Toast.LENGTH_LONG).show()
                    Log.d("categoria debug",errorMsg)
                    }

                } else {
                    // Si el servidor tronó (Error 500, 404)
                    Toast.makeText(this@HomeActivity, "Error del server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR_MENU", "El server se murió: ${response.errorBody()?.string()}")
                }
            }

            // SI EL CARTERO NI LLEGÓ
            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "No hay netWORK, pa: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("NETWORK_ERROR_MENU", "Falló Retrofit", t)

            }
        })
    }
}