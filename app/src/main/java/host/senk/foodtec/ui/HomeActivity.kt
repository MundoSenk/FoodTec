package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // <-- ¡¡EL ÚNICO IMPORT NUEVO!!
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

        // JALAMOS EL SALUDO
        val tvBienvenido: TextView = findViewById(R.id.tvBienvenido)
        tvBienvenido.text = "Bienvenido, $nombreDelVato"

        // AMARRAMOS LOS RECYCLERVIEWS (¡Los "michis"!)
        val rvComida: RecyclerView = findViewById(R.id.rvComida)
        val rvBebidas: RecyclerView = findViewById(R.id.rvBebidas)
        rvComida.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvBebidas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // ¡JALAMOS LOS DATOS!
        cargarMenu("Comida", rvComida)
        cargarMenu("Bebida", rvBebidas)




        // AMARRAMOS LOS BOTONES DE MÓDULOS (Los cuadros de tu Figma)
               val btnModuloFoodter: CardView = findViewById(R.id.btnOpcion1) // El naranja
        val btnModuloObjetos: CardView = findViewById(R.id.btnOpcion2) // El azul

        // EL OÍDO PA'L BOTÓN NARANJA (FOODTERS)
        btnModuloFoodter.setOnClickListener {
            // ¡Checamos el "archivero" pa' ver si ya es Foodter!
            if (SessionManager.isFoodter(this)) {
                // ¡YA ES FOODTER! ¡A la pantalla de chamba!
                Log.d("HomeActivity", "El usuario SÍ es Foodter. Llevando a HomeFooterActivity.")

                Toast.makeText(this, "¡Ya eres Foodter! (Abriendo HomeFooter...)", Toast.LENGTH_SHORT).show()
                // val intent = Intent(this, HomeFooterActivity::class.java)
                // startActivity(intent)
            } else {
                // ES NUEVO! ¡Al formulario de registro!
                Log.d("HomeActivity", "El usuario NO es Foodter. Llevando a SignupFoodterActivity.")

                Toast.makeText(this, "¡Aún no eres Foodter! (Abriendo Registro...)", Toast.LENGTH_SHORT).show()
                 val intent = Intent(this, SignupFoodterActivity::class.java)
                 startActivity(intent)
            }
        }

        // El oído pa'l botón azul (Objetos Perdidos)
        btnModuloObjetos.setOnClickListener {
            // Pa'l futuro! (La 3/3 parte)
            Toast.makeText(this, "¡Módulo de Objetos Perdidos próximamente!", Toast. LENGTH_SHORT).show()
        }

        //


        // Este es tu código del buscador
        val etBuscadorFalso: EditText = findViewById(R.id.etBuscador)
        etBuscadorFalso.isFocusable = false
        etBuscadorFalso.isClickable = true

        etBuscadorFalso.setOnClickListener {
            // Abrimos la Activity de Búsqueda
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        // AQUÍ ESTÁ EL JALE DE USABILIDAD
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)

        // Le decimos que "Inicio" está seleccionado
        bottomNavView.selectedItemId = R.id.nav_home


        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {

                    true
                }

                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
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
    }


    // función cargarMenu i
    private fun cargarMenu(categoria: String, recyclerView: RecyclerView) {
        val call = RetrofitClient.apiService.obtenerMenu(categoria)
        call.enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val menuRespuesta = response.body()!!
                    if (menuRespuesta.status == "exito") {

                        menuRespuesta.menu?.let { listaDeMenu ->


                            // Aquí es donde le volvemos a pasar el chisme al DetailsActivity
                            val listenerDelClick = { comidaItem: ComidaItem ->
                                val intent = Intent(this@HomeActivity, DetailsActivity::class.java)

                                // Le mandamos el productp
                                intent.putExtra("COMIDA_SELECCIONADA", comidaItem)


                                // Le mandamos QUIÉN ES EL VATO
                                intent.putExtra("USER_NAME", usuarioLogueado)

                                startActivity(intent)
                            }

                            // Armamos el Adapter con la lista Y el listener
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
                // ¡El Toast de "No hay net"!)
            }
        })
    }
}