package host.senk.foodtec.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log // <-- ¡AÑADIDO!
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import host.senk.foodtec.R
import host.senk.foodtec.api.ApiService // <-- ¡AÑADIDO!
import host.senk.foodtec.api.RetrofitClient // <-- ¡AÑADIDO!
import host.senk.foodtec.manager.SessionManager // <-- ¡AÑADIDO!
import host.senk.foodtec.model.CrearPedidoResponse // <-- ¡AÑADIDO!
import host.senk.foodtec.model.Pedido // ¡El molde "gordo"!
import host.senk.foodtec.model.PedidoDetalle
import retrofit2.Call // <-- ¡AÑADIDO!
import retrofit2.Callback // <-- ¡AÑADIDO!
import retrofit2.Response // <-- ¡AÑADIDO!

class PedidoEnCursoActivity : AppCompatActivity() {

    // --- Vistas ---
    private lateinit var tvTitulo: TextView
    private lateinit var tvClienteNombre: TextView
    private lateinit var tvClienteLugar: TextView
    private lateinit var llDetallesContainer: LinearLayout
    private lateinit var btnEnCamino: Button
    private lateinit var btnEntregado: Button

    private var pedidoActual: Pedido? = null
    private lateinit var foodterId: String // ¡Guardaremos el ID del Foodter aquí!

    // --- API ---
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido_en_curso)

        // 1. ¡"Cachamos" el pedido que nos aventó la Activity anterior!
        pedidoActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("PEDIDO_ACEPTADO", Pedido::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("PEDIDO_ACEPTADO")
        }

        // 2. ¡"Cachamos" al Foodter!
        val id = SessionManager.getUserId(this)

        // ¡Si no hay pedido O no hay foodter, nos vamos!
        if (pedidoActual == null || id == null) {
            Toast.makeText(this, "Error: No se pudo cargar el pedido o el Foodter", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        foodterId = id // Guardamos el ID del Foodter

        // 3. Amarramos Vistas
        bindViews()

        // 4. ¡Pintamos la info!
        pintarInfoPedido()

        // 5. "Alambramos" los botones
        setupListeners()
    }

    private fun bindViews() {
        tvTitulo = findViewById(R.id.tvTituloPedidoCurso)
        tvClienteNombre = findViewById(R.id.tvClienteNombre)
        tvClienteLugar = findViewById(R.id.tvClienteLugar)
        llDetallesContainer = findViewById(R.id.llDetallesContainer)
        btnEnCamino = findViewById(R.id.btnMarcarEnCamino)
        btnEntregado = findViewById(R.id.btnMarcarEntregado)
    }

    /**
     * ¡La "carnita"! ¡Pinta los datos del Pedido!
     */
    private fun pintarInfoPedido() {
        pedidoActual?.let { pedido ->
            tvTitulo.text = "Pedido: #${pedido.id_pedido}"
            tvClienteNombre.text = pedido.nombre_cliente ?: "Cliente no encontrado"
            tvClienteLugar.text = "Lugar: ${pedido.lugar_entrega ?: "N/A"}"

            llDetallesContainer.removeAllViews()
            val inflater = LayoutInflater.from(this)

            pedido.detalles?.filterNotNull()?.forEach { detalle ->
                val itemVista = inflater.inflate(R.layout.item_pedido_actual, llDetallesContainer, false)

                itemVista.findViewById<View>(R.id.ivItemImagen).visibility = View.GONE

                val tvNombre: TextView = itemVista.findViewById(R.id.tvItemNombre)
                val tvCantidad: TextView = itemVista.findViewById(R.id.tvItemCantidad)
                val tvDetallesUser: TextView = itemVista.findViewById(R.id.tvItemDetallesUser)
                val tvPrecio: TextView = itemVista.findViewById(R.id.tvItemPrecioTotal)

                tvNombre.text = detalle.nombre ?: "?"
                tvCantidad.text = "Cantidad: x${detalle.cantidad ?: 0}"
                tvPrecio.visibility = View.GONE

                if (!detalle.detalles_usuario.isNullOrEmpty()) {
                    tvDetallesUser.visibility = View.VISIBLE
                    tvDetallesUser.text = "Notas: ${detalle.detalles_usuario}"
                } else {
                    tvDetallesUser.visibility = View.GONE
                }

                llDetallesContainer.addView(itemVista)
            }
        }
    }

    /**
     * ¡¡LA FUNCIÓN "ALAMBRADA"!!
     */
    private fun setupListeners() {

        btnEnCamino.setOnClickListener {
            // ¡Llamamos a la función mágica!
            llamarApiEstatus("En camino")
        }

        btnEntregado.setOnClickListener {
            // ¡Llamamos a la función mágica!
            llamarApiEstatus("Entregado")
        }
    }

    /**
     * ¡¡LA FUNCIÓN "MÁGICA" QUE LLAMA AL PHP!!
     */
    private fun llamarApiEstatus(nuevoEstatus: String) {
        val pedidoId = pedidoActual?.id_pedido
        if (pedidoId == null) {
            Toast.makeText(this, "Error: ID de pedido perdido", Toast.LENGTH_SHORT).show()
            return
        }

        // ¡Desactivamos botones pa' que no piquen doble!
        btnEnCamino.isEnabled = false
        btnEntregado.isEnabled = false
        btnEntregado.text = "ACTUALIZANDO..."

        Log.d("PedidoEnCurso", "Actualizando Pedido #$pedidoId a '$nuevoEstatus' por Foodter '$foodterId'")

        apiService.actualizarEstatusPedido(foodterId, pedidoId, nuevoEstatus)
            .enqueue(object: Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    val resp = response.body()
                    if (response.isSuccessful && resp?.status == "exito") {
                        // --- ¡¡ÉXITO!! ---
                        Toast.makeText(this@PedidoEnCursoActivity, "¡Pedido actualizado a: $nuevoEstatus!", Toast.LENGTH_SHORT).show()

                        if (nuevoEstatus == "Entregado") {
                            // ¡Si ya lo entregó, cerramos esta pantalla!
                            // (Y regresamos al HomeFoodter, que se refrescará solo)
                            finish()
                        } else {
                            // Si solo fue "En Camino", dejamos la pantalla abierta
                            // y ocultamos el botón de "En Camino"
                            btnEnCamino.visibility = View.GONE
                            // Reactivamos el de "Entregado"
                            btnEntregado.isEnabled = true
                            btnEntregado.text = "MARCAR COMO ENTREGADO"
                        }

                    } else {
                        // El PHP tronó (ej. "Error de autorización")
                        val errorMsg = resp?.mensaje ?: "Error del servidor"
                        Toast.makeText(this@PedidoEnCursoActivity, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                        // Reactivamos botones
                        btnEnCamino.isEnabled = true
                        btnEntregado.isEnabled = true
                        btnEntregado.text = "MARCAR COMO ENTREGADO"
                    }
                }

                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    Log.e("PedidoEnCurso", "Fallo de red", t)
                    Toast.makeText(this@PedidoEnCursoActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                    // Reactivamos botones
                    btnEnCamino.isEnabled = true
                    btnEntregado.isEnabled = true
                    btnEntregado.text = "MARCAR COMO ENTREGADO"
                }
            })
    }
}