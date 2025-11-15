package host.senk.foodtec.ui

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


import android.widget.ArrayAdapter // ¡Pal Spinner!
import android.widget.RadioGroup // ¡Pal RadioGroup!
import android.widget.Spinner // ¡Pal Spinner!
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager // ¡ARCHIVERO
import host.senk.foodtec.model.CartItemRequest
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.PedidoRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartModalFragment : BottomSheetDialogFragment() {

    // ¡Variables pa' guardar los datos chidos!
    private var subtotal: Double = 0.0
    private var comision: Double = 5.00 // ¡El 5.00 a mano!
    private var total: Double = 0.0

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

        // --- XML ---
        val rvItems: RecyclerView = view.findViewById(R.id.rvModalItems)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
        val tvComision: TextView = view.findViewById(R.id.tvComision)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalPagar)
        val spinnerLugar: Spinner = view.findViewById(R.id.spinnerLugar) // ¡EL SPINNER!
        val rgMetodoPago: RadioGroup = view.findViewById(R.id.rgMetodoPago) // ¡EL RADIO GROUP!
        val btnConfirmar: Button = view.findViewById(R.id.btnConfirmarCompra)
        val btnAgregarMas: Button = view.findViewById(R.id.btnAgregarMas)

        // JALAMOS LOS DATOS DEL CARRITO EL SINGLETON
        val itemsDelCarrito = CartManager.getItems()
        subtotal = CartManager.getSubtotal()
        total = subtotal + comision

        // TOTALES
        tvSubtotal.text = "Subtotal: $${String.format("%.2f", subtotal)}"
        tvComision.text = "Comisión de entrega: $${String.format("%.2f", comision)}"
        tvTotal.text = "TOTAL A PAGAR: $${String.format("%.2f", total)}"

        // CONECTAMOS EL RECYCLERVIEW
        rvItems.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CartAdapter(itemsDelCarrito) // ¡Le pasamos los items!
        rvItems.adapter = adapter // ¡Pum!

        // --- ¡¡AQUÍ RELLENAMOS EL SPINNER (A MANO)!! ---
        // (¡'requireContext()' es el "Contexto" de un Fragment!)
        val lugares = arrayOf("Selecciona un lugar...", "Edificio A", "Edificio B", "Edificio C", "Rectoría")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lugares)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLugar.adapter = spinnerAdapter


        // El de Agregar más
        btnAgregarMas.setOnClickListener {
            dismiss() // Cierra el modal
            activity?.finish()
        }


        // ¡El de CONFIRMAR
        btnConfirmar.setOnClickListener {

            // 1. ¡Jalamos los datos del formulario!
            val lugar = spinnerLugar.selectedItem.toString()
            val metodoPagoId = rgMetodoPago.checkedRadioButtonId
            val metodoPago = if (metodoPagoId == R.id.rbEfectivo) "Efectivo" else "Tarjeta"


            // Jalamos al vato del "ARCHIVERO
            val usuarioId = SessionManager.getUserId(requireContext())

            // alidaciones
            if (usuarioId == null) {
                Toast.makeText(requireContext(), "¡Error fatal! No se encontró al usuario.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (lugar == "Selecciona un lugar...") {
                Toast.makeText(requireContext(), "¡Oye! Elige un lugar de entrega.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
                costo_final = total,
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
                            dismiss() //



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
}