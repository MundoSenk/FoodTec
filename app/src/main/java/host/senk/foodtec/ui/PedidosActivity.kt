package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import host.senk.foodtec.R
import host.senk.foodtec.adapter.PedidoActualAdapter
import host.senk.foodtec.adapter.PedidosAdapter
import host.senk.foodtec.api.ApiService
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.Pedido
import host.senk.foodtec.model.PedidoDetalle
import host.senk.foodtec.model.PedidosResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import host.senk.foodtec.adapter.ValoracionItemsAdapter

class PedidosActivity : AppCompatActivity() {

    // --- Vistas ---
    private lateinit var btnRegresar: ImageButton
    private lateinit var bottomNavView: BottomNavigationView

    private lateinit var llPedidoActualContainer: LinearLayout
    private lateinit var tvNoPedidoActual: TextView
    private lateinit var tvPedidoActualId: TextView
    private lateinit var tvPedidoActualEstatus: TextView
    private lateinit var rvPedidoActualItems: RecyclerView
    private lateinit var tvPedidoActualTotal: TextView
    private lateinit var tvPedidoActualMetodo: TextView
    private lateinit var tvPedidoActualLugar: TextView
    private lateinit var rvPedidosAnteriores: RecyclerView

   ///Adapters
    private lateinit var pedidosAdapter: PedidosAdapter
    private lateinit var pedidoActualAdapter: PedidoActualAdapter
    private val listaPedidosAnteriores = mutableListOf<Pedido>()
    private val listaPedidoActualDetalles = mutableListOf<PedidoDetalle>()

    // API
    private val apiService: ApiService by lazy { RetrofitClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedidos)

        bindViews()
        setupRecyclerViewAnteriores()
        setupRecyclerViewActual()
        setupListeners()
        setupBottomNav()
        cargarPedidosDelUsuario()
    }

    private fun bindViews() {
        btnRegresar = findViewById(R.id.btnRegresar)
        bottomNavView = findViewById(R.id.bottomNavView)
        llPedidoActualContainer = findViewById(R.id.llPedidoActualContainer)
        tvNoPedidoActual = findViewById(R.id.tvNoPedidoActual)
        tvPedidoActualId = findViewById(R.id.tvPedidoActualId)
        tvPedidoActualEstatus = findViewById(R.id.tvPedidoActualEstatus)
        rvPedidoActualItems = findViewById(R.id.rvPedidoActualItems)
        tvPedidoActualTotal = findViewById(R.id.tvPedidoActualTotal)
        tvPedidoActualMetodo = findViewById(R.id.tvPedidoActualMetodo)
        tvPedidoActualLugar = findViewById(R.id.tvPedidoActualLugar)
        rvPedidosAnteriores = findViewById(R.id.rvPedidosAnteriores)
    }

    private fun setupRecyclerViewAnteriores() {
        // AQUI DEFINIMOS QUÉ PASA CUANDO TOCAN UN PEDIDO VIEJ
        pedidosAdapter = PedidosAdapter(listaPedidosAnteriores) { pedido ->

            // Solo dejamos calificar si ya está entregado
            if (pedido.estatus.equals("Entregado", ignoreCase = true)) {
                val calificacionYaDada = pedido.valoracion_cliente?.toFloatOrNull() ?: 0f

                if (calificacionYaDada > 0) {
                    // SI YA TIENE PUNTOS
                    Toast.makeText(this, "¡Ya calificaste este pedido! Gracias.", Toast.LENGTH_SHORT).show()
                } else {
                    // SI ES 0 VOTA
                    mostrarDialogoCalificacion(pedido)
                }
            } else {
                Toast.makeText(this, "Solo puedes calificar pedidos entregados", Toast.LENGTH_SHORT).show()
            }
        }

        rvPedidosAnteriores.layoutManager = LinearLayoutManager(this)
        rvPedidosAnteriores.adapter = pedidosAdapter
        rvPedidosAnteriores.isNestedScrollingEnabled = false
    }

    private fun setupRecyclerViewActual() {
        pedidoActualAdapter = PedidoActualAdapter(listaPedidoActualDetalles)
        rvPedidoActualItems.layoutManager = LinearLayoutManager(this)
        rvPedidoActualItems.adapter = pedidoActualAdapter
        rvPedidoActualItems.isNestedScrollingEnabled = false
    }

    private fun setupListeners() {
        btnRegresar.setOnClickListener { val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()}
    }

    private fun setupBottomNav() {
        bottomNavView.selectedItemId = R.id.nav_pedidos
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_pedidos -> true
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarPedidosDelUsuario() {
        val usuarioId = SessionManager.getUserId(this)
        if (usuarioId == null) {
            finish()
            return
        }

        apiService.obtenerMisPedidos(usuarioId).enqueue(object : Callback<PedidosResponse> {
            override fun onResponse(call: Call<PedidosResponse>, response: Response<PedidosResponse>) {
                if (response.isSuccessful) {
                    val respuesta = response.body()
                    if (respuesta?.status == "exito" && respuesta.pedidos != null) {
                        procesarListaDePedidos(respuesta.pedidos)
                    } else {
                        llPedidoActualContainer.visibility = View.GONE
                        tvNoPedidoActual.visibility = View.VISIBLE
                    }
                } else {
                    llPedidoActualContainer.visibility = View.GONE
                    tvNoPedidoActual.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<PedidosResponse>, t: Throwable) {
                Toast.makeText(this@PedidosActivity, "Error de conexión", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun procesarListaDePedidos(todosLosPedidos: List<Pedido>) {
        // Buscamos Pedido Actual
        val pedidoActual = todosLosPedidos.find {
            it.estatus.equals("Pendiente", ignoreCase = true) ||
                    it.estatus.equals("En preparacion", ignoreCase = true) ||
                    it.estatus.equals("En camino", ignoreCase = true)
        }

        if (pedidoActual != null) {
            SessionManager.setHasActiveOrder(this, true)
            tvNoPedidoActual.visibility = View.GONE
            llPedidoActualContainer.visibility = View.VISIBLE

            tvPedidoActualId.text = "Pedido: #${pedidoActual.id_pedido}"
            tvPedidoActualEstatus.text = (pedidoActual.estatus ?: "Desconocido").uppercase()
            tvPedidoActualTotal.text = "$${pedidoActual.costo_final ?: "0.00"}"
            tvPedidoActualMetodo.text = "Pago: ${pedidoActual.metodo_pago ?: "N/A"}"
            tvPedidoActualLugar.text = "Entrega: ${pedidoActual.lugar_entrega ?: "N/A"}"

            listaPedidoActualDetalles.clear()
            pedidoActual.detalles?.let { listaPedidoActualDetalles.addAll(it.filterNotNull()) }
            pedidoActualAdapter.notifyDataSetChanged()

        } else {
            SessionManager.setHasActiveOrder(this, false)
            llPedidoActualContainer.visibility = View.GONE
            tvNoPedidoActual.visibility = View.VISIBLE
        }

        //  Filtramos Pedidos Anteriores
        val pedidosAnteriores = todosLosPedidos.filter {
            it.estatus.equals("Entregado", ignoreCase = true) ||
                    it.estatus.equals("Cancelado", ignoreCase = true)
        }.sortedByDescending { it.id_pedido }

        listaPedidosAnteriores.clear()
        listaPedidosAnteriores.addAll(pedidosAnteriores)
        pedidosAdapter.notifyDataSetChanged()
    }

    // FUNCIONES PARA EL MODAL

    private fun mostrarDialogoCalificacion(pedido: Pedido) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        // Inflamos el layout del modal que creamos
        val view = layoutInflater.inflate(R.layout.dialog_valorar_pedido, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val rvItems = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvValorarItems)

        // Configuramos el adapter nuevo con los detalles del pedido
        // OJO: Asegúrate que 'pedido.detalles' no sea nulo
        val listaDetalles = pedido.detalles?.filterNotNull() ?: emptyList()
        val adapterItems = ValoracionItemsAdapter(listaDetalles)
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = adapterItems

        val rb = view.findViewById<android.widget.RatingBar>(R.id.rbDialogValoracion)
        val btn = view.findViewById<android.widget.Button>(R.id.btnEnviarCalificacion)

        btn.setOnClickListener {
            val estrellasFoodter = rb.rating


            val mapaCalificaciones = adapterItems.calificaciones
            val jsonBuilder = StringBuilder("[")
            var primero = true

            // --- CAMBIA ESTO (Sintaxis de desestructuración) ---
            mapaCalificaciones.forEach { (pos, rating) ->

                // Validamos que la posición exista en la lista (por seguridad)
                if (pos < listaDetalles.size) {
                    val item = listaDetalles[pos]
                    // OJO: Aquí usamos el alimento_id que agregamos al modelo
                    val idAlimento = item.alimento_id ?: 0

                    if (idAlimento > 0) {
                        if (!primero) jsonBuilder.append(",")
                        jsonBuilder.append("{\"id\":$idAlimento, \"puntos\":$rating}")
                        primero = false
                    }
                }
            }
            jsonBuilder.append("]")
            val itemsJson = jsonBuilder.toString()

            if (estrellasFoodter == 0f) {
                Toast.makeText(this, "¡Califica al Foodter porfa!", Toast.LENGTH_SHORT).show()
            } else {
                // 3. Enviamos todo
                enviarCalificacionAPI(pedido.id_pedido ?: 0, estrellasFoodter, itemsJson, dialog)
            }
        }
        dialog.show()
    }

    private fun enviarCalificacionAPI(pedidoId: Int, estrellas: Float, itemsJson: String, dialog: androidx.appcompat.app.AlertDialog) {
        val usuarioId = SessionManager.getUserId(this) ?: return

        apiService.calificarPedido(pedidoId, usuarioId, estrellas, "cliente", itemsJson)
            .enqueue(object : Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    if (response.isSuccessful && response.body()?.status == "exito") {
                        Toast.makeText(this@PedidosActivity, "¡Calificación enviada!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        cargarPedidosDelUsuario()
                    } else {
                        val msg = response.body()?.mensaje ?: "Error al calificar"
                        Toast.makeText(this@PedidosActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    Toast.makeText(this@PedidosActivity, "Fallo de red", Toast.LENGTH_SHORT).show()
                }
            })
    }
}