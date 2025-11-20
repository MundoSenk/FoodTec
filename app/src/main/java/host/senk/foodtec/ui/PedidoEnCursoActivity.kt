package host.senk.foodtec.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import host.senk.foodtec.R
import host.senk.foodtec.api.ApiService
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.Pedido
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidoEnCursoActivity : AppCompatActivity() {

    // XML
    private lateinit var tvTitulo: TextView
    private lateinit var tvClienteNombre: TextView
    private lateinit var tvClienteLugar: TextView
    private lateinit var llDetallesContainer: LinearLayout
    private lateinit var btnEnCamino: Button
    private lateinit var btnEntregado: Button

    private var pedidoActual: Pedido? = null
    private lateinit var foodterId: String

    // API
    private val apiService: ApiService by lazy { RetrofitClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido_en_curso)

        pedidoActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("PEDIDO_ACEPTADO", Pedido::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("PEDIDO_ACEPTADO")
        }

        val id = SessionManager.getUserId(this)
        if (pedidoActual == null || id == null) {
            Toast.makeText(this, "Error de datos", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        foodterId = id

        bindViews()
        pintarInfoPedido()
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

    private fun pintarInfoPedido() {
        pedidoActual?.let { pedido ->
            tvTitulo.text = "Pedido: #${pedido.id_pedido}"
            tvClienteNombre.text = pedido.nombre_cliente ?: "Cliente no encontrado"
            tvClienteLugar.text = "Lugar: ${pedido.lugar_entrega ?: "N/A"}"

            llDetallesContainer.removeAllViews()
            val inflater = LayoutInflater.from(this)

            pedido.detalles?.filterNotNull()?.forEach { detalle ->
                val itemVista = inflater.inflate(R.layout.item_pedido_actual, llDetallesContainer, false)
                itemVista.findViewById<View>(R.id.ivItemImagen).visibility = View.GONE // Ocultamos foto pa ahorrar espacio

                val tvNombre: TextView = itemVista.findViewById(R.id.tvItemNombre)
                val tvCantidad: TextView = itemVista.findViewById(R.id.tvItemCantidad)
                val tvDetallesUser: TextView = itemVista.findViewById(R.id.tvItemDetallesUser)
                val tvPrecio: TextView = itemVista.findViewById(R.id.tvItemPrecioTotal)

                tvNombre.text = detalle.nombre ?: "?"
                tvCantidad.text = "x${detalle.cantidad ?: 0}"
                tvPrecio.visibility = View.GONE // Foodter no necesita ver precios individuales aquí

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

    private fun setupListeners() {
        btnEnCamino.setOnClickListener { llamarApiEstatus("En camino") }
        btnEntregado.setOnClickListener { llamarApiEstatus("Entregado") }
    }

    private fun llamarApiEstatus(nuevoEstatus: String) {
        val pedidoId = pedidoActual?.id_pedido ?: return
        btnEnCamino.isEnabled = false
        btnEntregado.isEnabled = false

        apiService.actualizarEstatusPedido(foodterId, pedidoId, nuevoEstatus)
            .enqueue(object: Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    val resp = response.body()

                    if (response.isSuccessful && resp?.status == "exito") {
                        Toast.makeText(this@PedidoEnCursoActivity, "Estatus: $nuevoEstatus", Toast.LENGTH_SHORT).show()

                        if (nuevoEstatus == "Entregado") {
                            // --- AQUÍ CAMBIA LA COSA ---
                            // NO cerramos. Abrimos el modal para calificar al cliente.
                            mostrarDialogoCalificarCliente(pedidoId)

                        } else {
                            // Si es "En camino", seguimos normal
                            btnEnCamino.visibility = View.GONE
                            btnEntregado.isEnabled = true
                            btnEntregado.text = "MARCAR COMO ENTREGADO"
                        }
                    } else {
                        Toast.makeText(this@PedidoEnCursoActivity, resp?.mensaje ?: "Error", Toast.LENGTH_SHORT).show()
                        btnEnCamino.isEnabled = true
                        btnEntregado.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    Toast.makeText(this@PedidoEnCursoActivity, "Error de red", Toast.LENGTH_SHORT).show()
                    btnEnCamino.isEnabled = true
                    btnEntregado.isEnabled = true
                }
            })
    }

    // NUEVAS FUNCIONES DE CALIFICACIÓN

    private fun mostrarDialogoCalificarCliente(pedidoId: Int) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_valorar_cliente, null)
        builder.setView(view)
        // Evitamos que lo cierren picando afuera, ¡que califiquen a wiwi!
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val rb = view.findViewById<android.widget.RatingBar>(R.id.rbDialogValoracionCliente)
        val btn = view.findViewById<Button>(R.id.btnEnviarCalificacionCliente)

        btn.setOnClickListener {
            val estrellas = rb.rating
            if (estrellas == 0f) {
                Toast.makeText(this, "¡Califícalo, no seas gacho!", Toast.LENGTH_SHORT).show()
            } else {
                enviarCalificacionFoodter(pedidoId, estrellas, dialog)
            }
        }
        dialog.show()
    }

    private fun enviarCalificacionFoodter(pedidoId: Int, estrellas: Float, dialog: AlertDialog) {
        // Rol = "foodter" porque NOSOTROS (el foodter) estamos calificando
        apiService.calificarPedido(pedidoId, foodterId, estrellas, "foodter")
            .enqueue(object : Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    dialog.dismiss()
                    // AHORA SÍ, MATAMOS LA ACTIVITY
                    Toast.makeText(this@PedidoEnCursoActivity, "¡Chamba terminada!", Toast.LENGTH_LONG).show()
                    finish()
                }

                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    // Si falla la red, igual cerramos pa' no trabar al foodter, ya se entregó el pedido
                    Toast.makeText(this@PedidoEnCursoActivity, "Se guardó localmente (error red)", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    finish()
                }
            })
    }
}