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
import com.google.android.material.switchmaterial.SwitchMaterial // ¡El del Toggle!
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
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.PedidoUnicoResponse // ¡El molde "Pro"!


class HomeFoodterActivity : AppCompatActivity() {

    // --- Vistas ---
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var rvPedidosDisponibles: RecyclerView
    private lateinit var tvNoPedidos: TextView
    private lateinit var switchModoChamba: SwitchMaterial

    // --- Lógica y Adapter ---
    private lateinit var pedidosAdapter: PedidosDisponiblesAdapter
    private val listaDePedidos = mutableListOf<Pedido>()
    private lateinit var foodterId: String // ¡Global!

    // API
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_foodter)

        // ¡Jalamos al vato del "archivero" PRIMERO!
        val id = SessionManager.getUserId(this)
        if (id == null) {
            Toast.makeText(this, "Error fatal: No se pudo identificar al Foodter", Toast.LENGTH_LONG).show()
            Log.e("HomeFoodterActivity", "¡El foodter_id es nulo! Saliendo.")
            finish()
            return
        }
        foodterId = id

        // Amarramos Vistas
        bindViews()

        //  Configuramos el RecyclerView (¡con la lógica "Anti-Return"!)
        setupRecyclerView()

        // Configuramos el BottomNav (¡con la lógica "Pro"!)
        setupBottomNav()

        // Configuramos el "interruptor"
        setupModoChambaToggle()

        // A jalar la chamba!
        cargarPedidosDisponibles()
    }

    private fun bindViews() {
        bottomNavView = findViewById(R.id.bottomNavViewFoodter)
        rvPedidosDisponibles = findViewById(R.id.rvPedidosDisponibles)
        tvNoPedidos = findViewById(R.id.tvNoPedidosDisponibles)
        switchModoChamba = findViewById(R.id.switchModoChamba)
    }

    /**
     * ¡¡LA FUNCIÓN "ANTI-RETURN" (v3.0)!!
     */
    private fun setupRecyclerView() {
        // ¡El "oído" del botón Aceptar!
        val listenerAceptar = { pedido: Pedido ->

            // ¡Jalamos el ID del Foodter (de nosotros mismos)!
            val foodterId = SessionManager.getUserId(this)

            // QUÍ ESTÁ EL ARREGLO (IF/ELSE)!
            if (foodterId == null) {
                // 1. Si el Foodter ID es nulo, mostramos error y NO HACEMOS NADA MÁS
                Toast.makeText(this, "Error fatal: No se te pudo identificar", Toast.LENGTH_LONG).show()
            } else {
                //  El Foodter ID SÍ existe. ¡Checamos el Pedido ID!
                // ¡¡AQUÍ ESTÁ LA VALIDACIÓN QUE FALTABA!!
                val pedidoId = pedido.id_pedido

                if (pedidoId == null) {
                    // 3. Si el Pedido ID es nulo, mostramos error y NO HACEMOS NADA MÁS
                    Toast.makeText(this, "Error: Este pedido tiene un ID inválido", Toast.LENGTH_SHORT).show()
                } else {

                    // --- ¡¡ÉXITO!! ¡LOS DOS IDs EXISTEN! ¡A LA API! ---

                    apiService.aceptarPedido(foodterId, pedidoId).enqueue(object: Callback<CrearPedidoResponse> {
                        override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                            val resp = response.body()
                            if (response.isSuccessful && resp?.status == "exito") {

                                // --- ¡¡ÉXITO!! ¡YA ES NUESTRO! ---
                                Toast.makeText(this@HomeFoodterActivity, "¡Pedido #${pedido.id_pedido} aceptado!", Toast.LENGTH_SHORT).show()

                                //  ¡Abrimos la nueva pantalla "Pedido en Curso"!
                                val intent = Intent(this@HomeFoodterActivity, PedidoEnCursoActivity::class.java)

                                // ¡Le "aventamos" el pedido completo!
                                intent.putExtra("PEDIDO_ACEPTADO", pedido)
                                startActivity(intent)

                                //  ¡Refrescamos la lista de chamba!
                                cargarPedidosDisponibles()

                            } else {
                                // El PHP tronó o (más probable) "nos lo ganaron"!
                                val errorMsg = resp?.mensaje ?: "Error del servidor"
                                Toast.makeText(this@HomeFoodterActivity, errorMsg, Toast.LENGTH_LONG).show()

                                // ¡Refrescamos la lista pa' que desaparezca el que nos ganaron!
                                cargarPedidosDisponibles()
                            }
                        }

                        override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                            Toast.makeText(this@HomeFoodterActivity, "Error de Red: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }

        // ¡Creamos el nuevo adapter! (Esta parte es igual)
        pedidosAdapter = PedidosDisponiblesAdapter(listaDePedidos, listenerAceptar)
        rvPedidosDisponibles.layoutManager = LinearLayoutManager(this)
        rvPedidosDisponibles.adapter = pedidosAdapter
    }


    /**
     * ¡¡LA FUNCIÓN "PRO" DEL NAV BAR (v2.0)!!
     */
    private fun setupBottomNav() {

        // ¡¡CORREGIDO!! ¡Usamos el ID del menú Foodter!
        bottomNavView.selectedItemId = R.id.nav_foodter_home

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // ¡¡CORREGIDO!!
                R.id.nav_foodter_home -> true // Ya estamos aquí

                // ¡¡NUEVA LÓGICA "PRO"!!
                R.id.nav_foodter_mipedido -> {
                    // ¡Llama al PHP "francotirador"!
                    llamarApiMiPedidoActivo()
                    true
                }

                R.id.nav_perfil -> {
                    // ¡Este ya jala!
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * ¡¡NUEVA FUNCIÓN "FRANCOTIRADOR"!!
     * Llama al PHP que busca si tenemos chamba activa.
     */
    private fun llamarApiMiPedidoActivo() {
        Log.d("HomeFoodterActivity", "Checando si hay chamba activa para $foodterId...")

        apiService.obtenerMiPedidoActivo(foodterId).enqueue(object: Callback<PedidoUnicoResponse> {
            override fun onResponse(call: Call<PedidoUnicoResponse>, response: Response<PedidoUnicoResponse>) {
                val resp = response.body()

                if (response.isSuccessful && resp != null) {

                    if (resp.status == "exito_encontrado" && resp.pedido != null) {
                        // ¡SÍ HAY CHAMBA
                        Log.d("HomeFoodterActivity", "¡Chamba activa encontrada! Abriendo PedidoEnCurso...")

                        // Abrimos la pantalla "Pedido en Curso"
                        val intent = Intent(this@HomeFoodterActivity, PedidoEnCursoActivity::class.java)

                        // Le "aventamos" el pedido que encontramos
                        intent.putExtra("PEDIDO_ACEPTADO", resp.pedido)
                        startActivity(intent)

                    } else if (resp.status == "exito_no_encontrado") {
                        // --- ¡NO HAY CHAMBA! ---
                        Log.d("HomeFoodterActivity", "No se encontró chamba activa.")
                        // AQUÍ CORRECCIÓN DE TOAST
                        Toast.makeText(this@HomeFoodterActivity, "No tienes ningún pedido activo.", Toast.LENGTH_SHORT).show()

                    } else {
                        // Error raro del PHP
                        // ¡¡AQUÍ CORRECCIÓN DE TOAST!!
                        Toast.makeText(this@HomeFoodterActivity, resp.mensaje ?: "Error raro del PHP", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // Error 500 o 404
                    // ¡¡AQUÍ CORRECCIÓN DE TOAST!!
                    Toast.makeText(this@HomeFoodterActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PedidoUnicoResponse>, t: Throwable) {
                // ¡¡AQUÍ CORRECCIÓN DE TOAST!!
                Toast.makeText(this@HomeFoodterActivity, "Error de Red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


    // (Tu función setupModoChambaToggle queda EXACTAMENTE IGUAL)
    private fun setupModoChambaToggle() {
        switchModoChamba.setOnCheckedChangeListener { _, isChecked ->
            val nuevoStatus = if (isChecked) "Activo" else "Inactivo"
            val toastMsg = if (isChecked) "¡A chambear!" else "¡A mimir!"
            switchModoChamba.isEnabled = false
            Log.d("HomeFoodterActivity", "Cambiando status a: $nuevoStatus")

            apiService.actualizarStatusFoodter(foodterId, nuevoStatus)
                .enqueue(object: Callback<CrearPedidoResponse> {
                    override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                        switchModoChamba.isEnabled = true

                        if (response.isSuccessful && response.body()?.status == "exito") {
                            // ¡¡AQUÍ CORRECCIÓN DE TOAST!!
                            Toast.makeText(this@HomeFoodterActivity, toastMsg, Toast.LENGTH_SHORT).show()
                            if (nuevoStatus == "Inactivo") {
                                tvNoPedidos.visibility = View.VISIBLE
                                rvPedidosDisponibles.visibility = View.GONE
                                tvNoPedidos.text = "Modo 'Inactivo'. ¡Actívate para ver la chamba!"
                            } else {
                                cargarPedidosDisponibles()
                            }
                        } else {
                            // ¡¡AQUÍ CORRECCIÓN DE TOAST!!
                            Toast.makeText(this@HomeFoodterActivity, "Error al cambiar de estado", Toast.LENGTH_SHORT).show()
                            switchModoChamba.isChecked = !isChecked
                        }
                    }

                    override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                        switchModoChamba.isEnabled = true
                        // ¡¡AQUÍ CORRECCIÓN DE TOAST!!
                        Toast.makeText(this@HomeFoodterActivity, "Error de red", Toast.LENGTH_SHORT).show()
                        switchModoChamba.isChecked = !isChecked
                    }
                })
        }
    }


    /**
     * ¡La función que llama al nuevo PHP!
     */
    private fun cargarPedidosDisponibles() {
        Log.d("HomeFoodterActivity", "Cargando pedidos disponibles...")

        apiService.obtenerPedidosDisponibles(foodterId).enqueue(object : Callback<PedidosResponse> {
            override fun onResponse(call: Call<PedidosResponse>, response: Response<PedidosResponse>) {
                if (response.isSuccessful && response.body()?.status == "exito") {
                    val pedidos = response.body()!!.pedidos

                    if (pedidos.isNullOrEmpty()) {
                        Log.d("HomeFoodterActivity", "No se encontraron pedidos pendientes.")
                        tvNoPedidos.visibility = View.VISIBLE
                        rvPedidosDisponibles.visibility = View.GONE
                        tvNoPedidos.text = "¡No hay chamba por ahora! \nDescansa, Foodter."
                    } else {
                        Log.d("HomeFoodterActivity", "Se encontraron ${pedidos.size} pedidos")
                        tvNoPedidos.visibility = View.GONE
                        rvPedidosDisponibles.visibility = View.VISIBLE

                        listaDePedidos.clear()
                        listaDePedidos.addAll(pedidos.filterNotNull())
                        pedidosAdapter.notifyDataSetChanged()
                    }
                } else {
                    val errorMsg = response.body()?.mensaje ?: "Error del servidor: ${response.code()}"
                    Log.e("HomeFoodterActivity", "Error al cargar pedidos: $errorMsg")
                    Toast.makeText(this@HomeFoodterActivity, errorMsg, Toast.LENGTH_LONG).show()
                    tvNoPedidos.visibility = View.VISIBLE
                    rvPedidosDisponibles.visibility = View.GONE
                    tvNoPedidos.text = "Error al cargar pedidos."
                }
            }

            override fun onFailure(call: Call<PedidosResponse>, t: Throwable) {
                Log.e("HomeFoodterActivity", "Fallo de red", t)
                Toast.makeText(this@HomeFoodterActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                tvNoPedidos.visibility = View.VISIBLE
                rvPedidosDisponibles.visibility = View.GONE
                tvNoPedidos.text = "Error de conexión."
            }
        })
    }
}