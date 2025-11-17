package host.senk.foodtec.ui

import android.content.Intent // <-- Ya lo teníamos
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout // <-- ¡¡Este ya no lo usamos!! Lo podemos borrar.
import android.widget.ImageButton
import android.widget.LinearLayout // <-- ¡¡NUEVO!!
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView // <-- Ya lo teníamos
import host.senk.foodtec.R
import host.senk.foodtec.adapter.PedidoActualAdapter // <-- ¡¡NUEVO!!
import host.senk.foodtec.adapter.PedidosAdapter
import host.senk.foodtec.api.ApiService
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.Pedido
import host.senk.foodtec.model.PedidoDetalle // <-- ¡¡NUEVO!!
import host.senk.foodtec.model.PedidosResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidosActivity : AppCompatActivity() {

    // --- Vistas del XML (¡Sección actualizada!) ---
    private lateinit var btnRegresar: ImageButton
    private lateinit var bottomNavView: BottomNavigationView

    // (Vistas "Pedido Actual" - ¡NUEVAS!)
    private lateinit var llPedidoActualContainer: LinearLayout
    private lateinit var tvNoPedidoActual: TextView
    private lateinit var tvPedidoActualId: TextView
    private lateinit var tvPedidoActualEstatus: TextView
    private lateinit var rvPedidoActualItems: RecyclerView // ¡El Recycler de detalles!
    private lateinit var tvPedidoActualTotal: TextView
    private lateinit var tvPedidoActualMetodo: TextView
    private lateinit var tvPedidoActualLugar: TextView

    // (Vistas "Pedidos Anteriores" - igual)
    private lateinit var rvPedidosAnteriores: RecyclerView

    // --- Lógica y Adapters ---
    private lateinit var pedidosAdapter: PedidosAdapter // Adapter para historial
    private lateinit var pedidoActualAdapter: PedidoActualAdapter // ¡¡NUEVO adapter pa' detalles!!

    private val listaPedidosAnteriores = mutableListOf<Pedido>()
    private val listaPedidoActualDetalles = mutableListOf<PedidoDetalle>() // ¡¡NUEVA lista!!

    // Conexión a la API (igual)
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos)

        // (conectar) las vistas del XML
        bindViews()

        // Configurar el RecyclerView de "Pedidos Anteriores"
        setupRecyclerViewAnteriores()

        // 3. Configurar el RecyclerView de "Pedido Actual
        setupRecyclerViewActual()

        // Configurar el botón de regresar
        setupListeners()

        // Configurar el BottomNav
        setupBottomNav()

        // Jalar los datos de la API
        cargarPedidosDelUsuario()
    }

    private fun bindViews() {
        btnRegresar = findViewById(R.id.btnRegresar)
        bottomNavView = findViewById(R.id.bottomNavView)

        // --- ¡¡xml!! ---
        llPedidoActualContainer = findViewById(R.id.llPedidoActualContainer)
        tvNoPedidoActual = findViewById(R.id.tvNoPedidoActual)
        tvPedidoActualId = findViewById(R.id.tvPedidoActualId)
        tvPedidoActualEstatus = findViewById(R.id.tvPedidoActualEstatus)
        rvPedidoActualItems = findViewById(R.id.rvPedidoActualItems)
        tvPedidoActualTotal = findViewById(R.id.tvPedidoActualTotal)
        tvPedidoActualMetodo = findViewById(R.id.tvPedidoActualMetodo)
        tvPedidoActualLugar = findViewById(R.id.tvPedidoActualLugar)

        // El de antes
        rvPedidosAnteriores = findViewById(R.id.rvPedidosAnteriores)
    }

    // Cambiamos nombre a Anteriores para que sea claro
    private fun setupRecyclerViewAnteriores() {
        pedidosAdapter = PedidosAdapter(listaPedidosAnteriores) { pedido ->
            Toast.makeText(this, "Viendo detalles del pedido #${pedido.id_pedido}", Toast.LENGTH_SHORT).show()
        }
        rvPedidosAnteriores.layoutManager = LinearLayoutManager(this)
        rvPedidosAnteriores.adapter = pedidosAdapter
        rvPedidosAnteriores.isNestedScrollingEnabled = false // Importante por el ScrollView
    }

    // NUEVA FUNCIÓN
    private fun setupRecyclerViewActual() {
        // Usamos el nuevo adapter y la nueva lista
        pedidoActualAdapter = PedidoActualAdapter(listaPedidoActualDetalles)

        rvPedidoActualItems.layoutManager = LinearLayoutManager(this)
        rvPedidoActualItems.adapter = pedidoActualAdapter
        rvPedidoActualItems.isNestedScrollingEnabled = false // ¡Importante por el ScrollView!
    }


    private fun setupListeners() {
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    // Esta función ya estaba del paso anterior
    private fun setupBottomNav() {
        bottomNavView.selectedItemId = R.id.nav_pedidos
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_pedidos -> true // Ya estamos aquí
                R.id.nav_perfil -> {
                    Toast.makeText(this, "¡Perfil próximamente!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }


    private fun cargarPedidosDelUsuario() {
        val usuarioId = SessionManager.getUserId(this)

        if (usuarioId == null) {
            Toast.makeText(this, "Error: No se pudo encontrar al usuario", Toast.LENGTH_LONG).show()
            Log.e("PedidosActivity", "El usuarioId es nulo. El vato no está logueado.")
            finish()
            return
        }

        Log.d("PedidosActivity", "Cargando pedidos para el usuario: $usuarioId")

        // Llamada a Retrofit! (Ahora jala el JSON MMAMADOO!)
        apiService.obtenerMisPedidos(usuarioId).enqueue(object : Callback<PedidosResponse> {
            override fun onResponse(call: Call<PedidosResponse>, response: Response<PedidosResponse>) {
                if (response.isSuccessful) {
                    val respuesta = response.body() 

                    // Validamos que el parseo funcionó Y que el PHP dijo "exito"
                    if (respuesta?.status == "exito" && respuesta.pedidos != null) {
                        Log.d("PedidosActivity", "Se recibieron ${respuesta.pedidos.size} pedidos (gordos)")
                        procesarListaDePedidos(respuesta.pedidos)

                    } else {
                        // QUÍ ESTABA EL ERROR DE UI
                        Log.w("PedidosActivity", "Respuesta de API nula o no exitosa: ${respuesta?.mensaje}")
                        Toast.makeText(this@PedidosActivity, respuesta?.mensaje ?: "No se encontraron pedidos", Toast.LENGTH_SHORT).show()

                        // MOSTRAMOS EL MENSAJE DE "NO HAY PEDIDOS
                        llPedidoActualContainer.visibility = View.GONE
                        tvNoPedidoActual.visibility = View.VISIBLE // ¡¡ESTO FALTABA!!
                    }
                } else {
                    Log.e("PedidosActivity", "Error en la respuesta del servidor: ${response.code()}")
                    Toast.makeText(this@PedidosActivity, "Error del servidor", Toast.LENGTH_SHORT).show()
                    // ¡También mostramos el error aquí!
                    llPedidoActualContainer.visibility = View.GONE
                    tvNoPedidoActual.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<PedidosResponse>, t: Throwable) {
                Log.e("PedidosActivity", "Fallo de red", t)
                Toast.makeText(this@PedidosActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * ¡¡LA CARNITA!! (Versión 2.0)
     * Esta función ahora controla la nueva UI.
     */
    private fun procesarListaDePedidos(todosLosPedidos: List<Pedido>) {

        // BUSCAR EL PEDIDO ACTUAL
        val pedidoActual = todosLosPedidos.find {
            it.estatus.equals("Pendiente", ignoreCase = true) ||
                    it.estatus.equals("En preparacion", ignoreCase = true) ||
                    it.estatus.equals("En camino", ignoreCase = true)
        }

        if (pedidoActual != null) {
            // SÍ HAY PEDIDO ACTUAL!
            Log.d("PedidosActivity", "Pedido actual encontrado: #${pedidoActual.id_pedido}")

            // Ocultamos el "No hay pedidos" y mostramos el contenedor mamalón
            tvNoPedidoActual.visibility = View.GONE
            llPedidoActualContainer.visibility = View.VISIBLE

            // Llenamos los TextViews de arriba y abajo
            tvPedidoActualId.text = "Pedido: #${pedidoActual.id_pedido}"
            tvPedidoActualEstatus.text = pedidoActual.estatus.uppercase() // ¡PENDIENTE!
            tvPedidoActualTotal.text = "$${pedidoActual.costo_final}"
            tvPedidoActualMetodo.text = "Pago: ${pedidoActual.metodo_pago}"
            tvPedidoActualLugar.text = "Lugar entrega: ${pedidoActual.lugar_entrega}"

            // Llenamos el RecyclerView de detalles
            listaPedidoActualDetalles.clear()
            listaPedidoActualDetalles.addAll(pedidoActual.detalles)
            pedidoActualAdapter.notifyDataSetChanged() // ¡El adapter nuevo chambeando!

        } else {
            // NO HAY PEDIDO ACTUAL
            Log.d("PedidosActivity", "No se encontraron pedidos activos.")

            // Ocultamos el contenedor mamalón y mostramos el "No hay pedidos"
            llPedidoActualContainer.visibility = View.GONE
            tvNoPedidoActual.visibility = View.VISIBLE
        }

        //  FILTRAR LOS PEDIDOS ANTERIORES -
        val pedidosAnteriores = todosLosPedidos.filter {
            it.estatus.equals("Entregado", ignoreCase = true) ||
                    it.estatus.equals("Cancelado", ignoreCase = true)
        }.sortedByDescending { it.id_pedido }

        //  ACTUALIZAR EL RECYCLERVIEW DE ANTERIORES
        listaPedidosAnteriores.clear()
        listaPedidosAnteriores.addAll(pedidosAnteriores)
        pedidosAdapter.notifyDataSetChanged() // El adapter viejo chambeando

        if (pedidosAnteriores.isEmpty()) {
            Log.d("PedidosActivity", "No hay pedidos anteriores para mostrar.")

        }
    }
}