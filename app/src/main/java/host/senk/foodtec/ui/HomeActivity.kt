package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import host.senk.foodtec.R
import host.senk.foodtec.adapter.MenuAdapter
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.CartManager
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.ComidaItem
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.MenuResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private var usuarioLogueado: String = "invitado"
    private var nombreDelVato: String = "Usuario"

    private lateinit var rvSnacks: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // JALAMOS EL "ARCHIVERO"
        usuarioLogueado = SessionManager.getUserId(this) ?: "invitado_error"
        nombreDelVato = SessionManager.getUserName(this) ?: "Usuario"

        // JALAMOS EL SALUDO
        val tvBienvenido: TextView = findViewById(R.id.tvBienvenido)
        tvBienvenido.text = "Bienvenido, $nombreDelVato"

        // AMARRAMOS LOS RECYCLERVIEWS
        val rvComida: RecyclerView = findViewById(R.id.rvComida)
        val rvBebidas: RecyclerView = findViewById(R.id.rvBebidas)
        rvSnacks = findViewById(R.id.rvSnacks)

        rvComida.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvBebidas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvSnacks.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // ¡JALAMOS LOS DATOS!
        cargarMenu("Comida", rvComida)
        cargarMenu("Bebida", rvBebidas)
        cargarMenu("Snack", rvSnacks)

        // AMARRAMOS LOS BOTONES DE MÓDULOS
        val btnModuloFoodter: CardView = findViewById(R.id.btnOpcion1) // El naranja
        val btnModuloObjetos: CardView = findViewById(R.id.btnOpcion2) // El azul

        // EL OÍDO PA'L BOTÓN NARANJA (FOODTERS)
        btnModuloFoodter.setOnClickListener {
            if (SessionManager.isFoodter(this)) {
                Log.d("HomeActivity", "El usuario SÍ es Foodter. Llevando a HomeFooterActivity.")
                Toast.makeText(this, "¡Ya eres Foodter! (Abriendo HomeFooter...)", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeFoodterActivity::class.java)
                startActivity(intent)
            } else {
                Log.d("HomeActivity", "El usuario NO es Foodter. Llevando a SignupFoodterActivity.")
                Toast.makeText(this, "¡Aún no eres Foodter! (Abriendo Registro...)", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignupFoodterActivity::class.java)
                startActivity(intent)
            }
        }

        // --- AQUÍ ESTÁ LA LÓGICA NUEVA DEL BOTÓN AZUL ---
        btnModuloObjetos.setOnClickListener {
            // 1. Checamos si ya tenemos el whats en el archivero
            val telefonoGuardado = SessionManager.getPhone(this)

            if (telefonoGuardado.isNullOrEmpty()) {
                // 2. NO TIENE -> ¡Pedirselo!
                mostrarDialogoPedirWhats()
            } else {
                // 3. YA TIENE -> ¡Pásele al Módulo Azul!
                abrirModuloObjetos()
            }
        }


        // BUSCADOR
        val etBuscadorFalso: EditText = findViewById(R.id.etBuscador)
        etBuscadorFalso.isFocusable = false
        etBuscadorFalso.isClickable = true
        etBuscadorFalso.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        // BOTTOM NAV
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
        bottomNavView.selectedItemId = R.id.nav_home

        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_pedidos -> {
                    if (CartManager.getItems().isNotEmpty()) {
                        val modal = CartModalFragment()
                        modal.show(supportFragmentManager, "MODAL_CARRITO")
                        return@setOnItemSelectedListener false
                    } else {
                        val intent = Intent(this, PedidosActivity::class.java)
                        startActivity(intent)
                        true
                    }
                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }


    // FUNCIONES DE CARGA DE MENÚ
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
                    } else {
                        Toast.makeText(this@HomeActivity, "Error del PHP: ${menuRespuesta.mensaje}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Error del server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "No hay net: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // MÓDULO DE OBJETOS PERDIDOS Y WHATSAPP

    private fun abrirModuloObjetos() {
        val intent = Intent(this, ObjetosPerdidosActivity::class.java)
        startActivity(intent)
    }

    private fun mostrarDialogoPedirWhats() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_pedir_whats, null)
        builder.setView(view)
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etWhats = view.findViewById<EditText>(R.id.etWhatsInput)
        val btnGuardar = view.findViewById<android.widget.Button>(R.id.btnGuardarWhats)
        val btnCancelar = view.findViewById<android.widget.Button>(R.id.btnCancelarWhats)

        btnGuardar.setOnClickListener {
            val numero = etWhats.text.toString().trim()
            if (numero.length < 10) {
                Toast.makeText(this, "Escribe un número válido (10 dígitos)", Toast.LENGTH_SHORT).show()
            } else {
                guardarWhatsEnNube(numero, dialog)
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun guardarWhatsEnNube(numero: String, dialog: androidx.appcompat.app.AlertDialog) {
        val userId = SessionManager.getUserId(this) ?: return

        RetrofitClient.apiService.actualizarTelefono(userId, numero)
            .enqueue(object : Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    if (response.isSuccessful && response.body()?.status == "exito") {
                        SessionManager.setPhone(this@HomeActivity, numero)
                        Toast.makeText(this@HomeActivity, "¡Número guardado!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        abrirModuloObjetos()
                    } else {
                        Toast.makeText(this@HomeActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Fallo de red", Toast.LENGTH_SHORT).show()
                }
            })
    }

} // <--- ¡ESTA LLAVE CIERRA LA CLASE!