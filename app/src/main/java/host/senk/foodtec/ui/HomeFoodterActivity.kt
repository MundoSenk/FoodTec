package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import host.senk.foodtec.R
import host.senk.foodtec.adapter.PedidosDisponiblesAdapter
import host.senk.foodtec.api.ApiService
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.model.Pedido
import host.senk.foodtec.model.PedidosResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import host.senk.foodtec.manager.SessionManager

class HomeFoodterActivity : AppCompatActivity() {

    // --- Vistas ---
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var rvPedidosDisponibles: RecyclerView
    private lateinit var tvNoPedidos: TextView

    // --- Lógica y Adapter ---
    private lateinit var pedidosAdapter: PedidosDisponiblesAdapter
    private val listaDePedidos = mutableListOf<Pedido>()

    // API
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_foodter)

        // Amarramos Vistas
        bindViews()

        //  Configuramos el RecyclerView (¡con el nuevo adapter!)
        setupRecyclerView()

        // Configuramos el BottomNav
        setupBottomNav()

        // A jalar la chamba!
        cargarPedidosDisponibles()
    }

    private fun bindViews() {
        bottomNavView = findViewById(R.id.bottomNavViewFoodter)
        rvPedidosDisponibles = findViewById(R.id.rvPedidosDisponibles)
        tvNoPedidos = findViewById(R.id.tvNoPedidosDisponibles)
    }

    private fun setupRecyclerView() {
        // ¡El "oído" del botón Aceptar!
        val listenerAceptar = { pedido: Pedido ->
            // ¡Aquí irá la lógica mamalona de aceptar el pedido!
            // (Por ahora, un Toast)
            Toast.makeText(this, "¡Aceptando pedido #${pedido.id_pedido}!", Toast.LENGTH_SHORT).show()

            // (Futuro: Llamar a un PHP 'aceptarPedido.php(pedido.id_pedido, foodter_id)')
            // (Futuro: Quitar el item de la lista y refrescar)
        }

        // Creamos el nuevo adapter
        pedidosAdapter = PedidosDisponiblesAdapter(listaDePedidos, listenerAceptar)

        rvPedidosDisponibles.layoutManager = LinearLayoutManager(this)
        rvPedidosDisponibles.adapter = pedidosAdapter
    }

    private fun setupBottomNav() {

        // Por ahora, lo dejamos en "Inicio" (o el que quieras)
        bottomNavView.selectedItemId = R.id.nav_home

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Ya estamos aquí (o esta es la Home del Foodter)
                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_pedidos -> {
                    // (Esta sería la pantalla de "Mis Pedidos Aceptados")
                    Toast.makeText(this, "¡Mis Pedidos (Foodter) próximamente!", Toast.LENGTH_LONG).show()
                    true
                }
                R.id.nav_perfil -> {
                    Toast.makeText(this, "¡Perfil (Foodter) próximamente!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * ¡La función que llama al nuevo PHP!
     */
    private fun cargarPedidosDisponibles() {
        Log.d("HomeFoodterActivity", "Cargando pedidos disponibles...")

        // Jalamos al vato del "archivero"
        val foodterId = SessionManager.getUserId(this)
        if (foodterId == null) {
            Toast.makeText(this, "Error fatal: No se pudo identificar al Foodter", Toast.LENGTH_LONG).show()
            Log.e("HomeFoodterActivity", "¡El foodter_id es nulo! Saliendo.")
            finish() // Lo sacamos si no hay ID
            return
        }

        // ¡Le mandamos el ID real!
        apiService.obtenerPedidosDisponibles(foodterId).enqueue(object : Callback<PedidosResponse> {
            override fun onResponse(call: Call<PedidosResponse>, response: Response<PedidosResponse>) {
                if (response.isSuccessful && response.body()?.status == "exito") {
                    val pedidos = response.body()!!.pedidos

                    if (pedidos.isNullOrEmpty()) {
                        // No hay chamba
                        Log.d("HomeFoodterActivity", "No se encontraron pedidos pendientes.")
                        tvNoPedidos.visibility = View.VISIBLE
                        rvPedidosDisponibles.visibility = View.GONE
                    } else {
                        // Sí hay chamba
                        Log.d("HomeFoodterActivity", "Se encontraron ${pedidos.size} pedidos")
                        tvNoPedidos.visibility = View.GONE
                        rvPedidosDisponibles.visibility = View.VISIBLE

                        listaDePedidos.clear()
                        listaDePedidos.addAll(pedidos.filterNotNull()) // ¡Antibala!
                        pedidosAdapter.notifyDataSetChanged()
                    }
                } else {
                    // Error del PHP o del server
                    val errorMsg = response.body()?.mensaje ?: "Error del servidor: ${response.code()}"
                    Log.e("HomeFoodterActivity", "Error al cargar pedidos: $errorMsg")
                    Toast.makeText(this@HomeFoodterActivity, errorMsg, Toast.LENGTH_LONG).show()
                    tvNoPedidos.visibility = View.VISIBLE
                    rvPedidosDisponibles.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<PedidosResponse>, t: Throwable) {
                Log.e("HomeFoodterActivity", "Fallo de red", t)
                Toast.makeText(this@HomeFoodterActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                tvNoPedidos.visibility = View.VISIBLE
                rvPedidosDisponibles.visibility = View.GONE
            }
        })
    }
}