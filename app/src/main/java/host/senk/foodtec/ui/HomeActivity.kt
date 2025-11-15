package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import host.senk.foodtec.R
import android.util.Log

// IMPORTS CHIDOS
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.adapter.MenuAdapter
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.model.MenuResponse
import host.senk.foodtec.model.ComidaItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// --- EL ARCHIVERO
import host.senk.foodtec.manager.SessionManager

class HomeActivity : AppCompatActivity() {

    // --- ¡¡AQUÍ VAMOS A GUARDAR AL VATO!! ---
    // (¡Pa' pasárselo al 'DetailsActivity' luego!)
    private var usuarioLogueado: String = "invitado"
    private var nombreDelVato: String = "Usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // ¡El '0' pa'l BottomNav!
            insets
        }



        // 1. ¡Jalamos el "archivero"!
        // (¡'this' es el "Contexto"!)
        usuarioLogueado = SessionManager.getUserId(this) ?: "invitado_error"
        nombreDelVato = SessionManager.getUserName(this) ?: "Usuario"

        // JALAMOS EL SALUDO
        val tvBienvenido: TextView = findViewById(R.id.tvBienvenido)
        // ¡Pintamos el nombre que SÍ jalamos del "archivero"!
        tvBienvenido.text = "Bienvenido, $nombreDelVato"

        // AMARRAMOS LOS RECYCLERVIEWS
        val rvComida: RecyclerView = findViewById(R.id.rvComida)
        val rvBebidas: RecyclerView = findViewById(R.id.rvBebidas)
        rvComida.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvBebidas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // JALAMOS LOS DATOS!
        cargarMenu("Comida", rvComida)
        cargarMenu("Bebida", rvBebidas)

    } // ¡AQUÍ TERMINA EL ONCREATE!



    private fun cargarMenu(categoria: String, recyclerView: RecyclerView) {

        val call = RetrofitClient.apiService.obtenerMenu(categoria)

        call.enqueue(object : Callback<MenuResponse> {

            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val menuRespuesta = response.body()!!

                    if (menuRespuesta.status == "exito") {

                        menuRespuesta.menu?.let { listaDeMenu ->


                            // (Aquí es donde le volvemos a pasar el chisme al DetailsActivity)
                            val listenerDelClick = { comidaItem: ComidaItem ->
                                val intent = Intent(this@HomeActivity, DetailsActivity::class.java)

                                // ¡Le mandamos el productp!
                                intent.putExtra("COMIDA_SELECCIONADA", comidaItem)


                                // Le mandamos QUIÉN ES EL VATO
                                intent.putExtra("USER_NAME", usuarioLogueado)

                                startActivity(intent)
                            }

                            // ¡Armamos el Adapter con la lista Y el listener!
                            val adapter = MenuAdapter(listaDeMenu, listenerDelClick)
                            recyclerView.adapter = adapter

                        } ?: run {
                            Toast.makeText(this@HomeActivity, "Error: El PHP dijo 'exito' pero no mandó menú", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        // Si el PHP nos bateó
                        val errorMsg = menuRespuesta.mensaje ?: "Error desconocido del PHP"
                        Toast.makeText(this@HomeActivity, "Error del PHP: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Si el servidor tronó
                    Toast.makeText(this@HomeActivity, "Error del server: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR_MENU", "El server se murió: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "No hay net, pa: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("NETWORK_ERROR_MENU", "Falló Retrofit", t)
            }
        })
    }
}