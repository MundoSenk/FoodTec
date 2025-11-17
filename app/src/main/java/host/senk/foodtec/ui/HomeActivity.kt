package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView // ¡El Menú de abajo!
import host.senk.foodtec.R
import host.senk.foodtec.adapter.MenuAdapter
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.CartManager // ¡Pa'l "botón inteligente"!
import host.senk.foodtec.manager.SessionManager // ¡El "archivero"!
import host.senk.foodtec.model.ComidaItem
import host.senk.foodtec.model.MenuResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private var usuarioLogueado: String = "invitado"
    private var nombreDelVato: String = "Usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home) //

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // ¡El '0' pa'l BottomNav!
            insets
        }

        // JALAMOS EL "ARCHIVERO
        usuarioLogueado = SessionManager.getUserId(this) ?: "invitado_error"
        nombreDelVato = SessionManager.getUserName(this) ?: "Usuario"

        // 2. JALAMOS EL SALUDO
        val tvBienvenido: TextView = findViewById(R.id.tvBienvenido)
        tvBienvenido.text = "Bienvenido, $nombreDelVato"

        // 3. AMARRAMOS LOS RECYCLERVIEWS (¡Los "michis"!)
        val rvComida: RecyclerView = findViewById(R.id.rvComida)
        val rvBebidas: RecyclerView = findViewById(R.id.rvBebidas)
        rvComida.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvBebidas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 4. ¡JALAMOS LOS DATOS!
        cargarMenu("Comida", rvComida)
        cargarMenu("Bebida", rvBebidas)

        // AQUÍ ESTÁ EL JALE DE USABILIDAD
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)

        // Le decimos que "Inicio" está seleccionado
        bottomNavView.selectedItemId = R.id.nav_home

        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {

                    true
                }

                R.id.nav_pedidos -> {
                    // ¡¡EL "BOTÓN INTELIGENTE"!!
                    if (CartManager.getItems().isNotEmpty()) {
                        // ¡Tiene cosas! ¡Abrimos el "Monstruo" (Modal)!
                        val modal = CartModalFragment()
                        modal.show(supportFragmentManager, "MODAL_CARRITO")
                        // ¡Lo regresamos a 'Home' pa' que no se vea feo!
                        return@setOnItemSelectedListener false

                    } else {
                        // ¡Vámonos a la Activity de "Estatus!

                        val intent = Intent(this, PedidosActivity::class.java)
                        startActivity(intent)
                        true
                    }
                }

                R.id.nav_perfil -> {
                    // ¡Pa'l futuro!
                    true
                }

                else -> false
            }
        }
    } // ¡Fin del onCreate!



    private fun cargarMenu(categoria: String, recyclerView: RecyclerView) {
        val call = RetrofitClient.apiService.obtenerMenu(categoria)
        call.enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val menuRespuesta = response.body()!!
                    if (menuRespuesta.status == "exito") {
                        menuRespuesta.menu?.let { listaDeMenu ->
                            val listenerDelClick = { comidaItem: ComidaItem ->
                                val intent = Intent(this@HomeActivity, DetailsActivity::class.java)
                                intent.putExtra("COMIDA_SELECCIONADA", comidaItem)
                                intent.putExtra("USER_NAME", usuarioLogueado)
                                startActivity(intent)
                            }
                            val adapter = MenuAdapter(listaDeMenu, listenerDelClick)
                            recyclerView.adapter = adapter
                        }
                    }
                }
            }
            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                // ¡El Toast de "No hay net"!)
            }
        })
    }
}