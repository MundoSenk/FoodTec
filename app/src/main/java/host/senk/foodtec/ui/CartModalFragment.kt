package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import host.senk.foodtec.R
import host.senk.foodtec.adapter.CartAdapter
import host.senk.foodtec.manager.CartManager
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.CartItem
import host.senk.foodtec.model.CartItemRequest
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.PedidoRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartModalFragment : BottomSheetDialogFragment() {

    ///XML
    private lateinit var rvItems: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvComision: TextView
    private lateinit var tvTotal: TextView
    private lateinit var spinnerLugar: Spinner
    private lateinit var rgMetodoPago: RadioGroup
    private lateinit var btnConfirmar: Button
    private lateinit var btnAgregarMas: Button

    private var comision: Double = 5.00 // ¡El 5.00 a mano!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Llenamos el xml
        val view = inflater.inflate(R.layout.fragment_cart_modal, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- AMARRAMOS EL XML ---
        rvItems = view.findViewById(R.id.rvModalItems)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvComision = view.findViewById(R.id.tvComision)
        tvTotal = view.findViewById(R.id.tvTotalPagar)
        spinnerLugar = view.findViewById(R.id.spinnerLugar)
        rgMetodoPago = view.findViewById(R.id.rgMetodoPago)
        btnConfirmar = view.findViewById(R.id.btnConfirmarCompra)
        btnAgregarMas = view.findViewById(R.id.btnAgregarMas)

        // --- AQUÍ RELLENAMOS EL SPINNER (A MANO)
        val lugares = arrayOf("Selecciona un lugar...", "Edificio A", "Edificio B", "Edificio C", "Rectoría")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lugares)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLugar.adapter = spinnerAdapter

        // JALE CHIDO EL REFRESH ---
        //
        refreshCartUI()

        // ¡El de Agregar más
        btnAgregarMas.setOnClickListener {
            dismiss() // Cierra el modal
            activity?.finish() // Mata el Details
        }

        // ¡El de CONFIRMAR
        btnConfirmar.setOnClickListener {
            // (¡El código de 'crearPedido' va aquí!)
            confirmarPedido()
        }
    }

    ////ARMADOOOO
    private fun refreshCartUI() {
        // JALAMOS LOS DATOS DEL CARRITO EL SINGLETON
        val itemsDelCarrito = CartManager.getItems()
        val subtotal = CartManager.getSubtotal()
        val total = subtotal + comision

        // TOTALES
        tvSubtotal.text = "Subtotal: $${String.format("%.2f", subtotal)}"
        tvComision.text = "Comisión de entrega: $${String.format("%.2f", comision)}"
        tvTotal.text = "TOTAL A PAGAR: $${String.format("%.2f", total)}"

        // ---  EL "LISTENER" PA' BORRAR
        val listenerPaBorrar = { item: CartItem ->
            CartManager.removeItem(item) // Lo borramos del "archivero"
            refreshCartUI() // Y "repintamos" todo de putazo
        }

        // CONECTAMOS EL RECYCLERVIEW
        rvItems.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CartAdapter(itemsDelCarrito, listenerPaBorrar) // Le pasamos los items Y el listener
        rvItems.adapter = adapter // ¡Pum!
    }

    /**
     * ¡¡EL JALE CHIDO  LA "CONFIRMACIÓN"!!
     */
    private fun confirmarPedido() {
        val itemsDelCarrito = CartManager.getItems() // ¡Jalamos los items otra vez!

        // Jalamos los datos del formulario
        val lugar = spinnerLugar.selectedItem.toString()
        val metodoPagoId = rgMetodoPago.checkedRadioButtonId
        val metodoPago = if (metodoPagoId == R.id.rbEfectivo) "Efectivo" else "Tarjeta"

        // Jalamos al vato del "ARCHIVERO
        val usuarioId = SessionManager.getUserId(requireContext())

        // Validaciones
        if (usuarioId == null) {
            Toast.makeText(requireContext(), "¡Error fatal! No se encontró al usuario.", Toast.LENGTH_SHORT).show()
            return
        }
        if (lugar == "Selecciona un lugar...") {
            Toast.makeText(requireContext(), "¡Oye! Elige un lugar de entrega.", Toast.LENGTH_SHORT).show()
            return
        }
        if (itemsDelCarrito.isEmpty()) {
            Toast.makeText(requireContext(), "¡Tu carrito está vacío, pa!", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertimos el Carrito "chido" al Carrito de API
        val itemsRequest = itemsDelCarrito.map { cartItem ->
            CartItemRequest(
                id = cartItem.id,
                cantidad = cartItem.cantidad,
                precio_unitario = cartItem.precio_unitario.toDoubleOrNull() ?: 0.0,
                detalles_usuario = cartItem.detalles_usuario
            )
        }

        // Armamos el "JSON MAMALÓN
        val pedidoRequest = PedidoRequest(
            usuario_id = usuarioId,
            lugar_entrega = lugar,
            costo_final = (CartManager.getSubtotal() + comision), // Lo recalculamos
            metodo_pago = metodoPago,
            items = itemsRequest
        )

        //  LLAMAR  (crearPedido.php)
        val call = RetrofitClient.apiService.crearPedido(pedidoRequest)

        call.enqueue(object: Callback<CrearPedidoResponse> {
            override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()!!

                    if (resp.status == "exito") {
                        // SE ARMÓ EL PEDIDO
                        Toast.makeText(requireContext(), resp.mensaje, Toast.LENGTH_LONG).show()
                        CartManager.clearCart() // ¡Limpiamos el carrito!

                        // Redireccionamos a PedidosActivity
                        val intent = Intent(activity, PedidosActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                        dismiss() // Cerramos el modal

                    } else if (resp.status == "error_limite") { // <-- ¡¡AQUÍ CACHAMOS EL ERROR NUEVO!!
                        Toast.makeText(requireContext(), resp.mensaje, Toast.LENGTH_LONG).show()

                    } else {
                        // Tronó el PHP (ej. "JSON inválido")
                        Toast.makeText(requireContext(), "Error del PHP: ${resp.mensaje}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Tronó el server (500, 404)!
                    Toast.makeText(requireContext(), "Error del server: ${response.code()}", Toast.LENGTH_LONG).show()
                    Log.e("API_ERROR_PEDIDO", "El server se murió: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                // ¡No hay net!
                Toast.makeText(requireContext(), "SIN CONEXIÓN AL SERVIDOR: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("NETWORK_ERROR_PEDIDO", "Falló Retrofit", t)
            }
        })
    }
}