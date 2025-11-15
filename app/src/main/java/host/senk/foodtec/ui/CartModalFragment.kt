package host.senk.foodtec.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import host.senk.foodtec.R
import host.senk.foodtec.adapter.CartAdapter // ¡El adapter nuevo!
import host.senk.foodtec.manager.CartManager // ¡El carrito!


import android.widget.Toast

class CartModalFragment : BottomSheetDialogFragment() {

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
        val btnConfirmar: Button = view.findViewById(R.id.btnConfirmarCompra)
        val btnAgregarMas: Button = view.findViewById(R.id.btnAgregarMas)

        // JALAMOS LOS DATOS DEL CARRITO EL SINGLETON
        val itemsDelCarrito = CartManager.getItems()
        val subtotal = CartManager.getSubtotal()
        val comision = 5.00 // ¡Este 5.00 lo pusiste a mano!
        val total = subtotal + comision

        // TOTALES (¡Ahora sí jala el 'tvComision'!)
        tvSubtotal.text = "Subtotal: $${String.format("%.2f", subtotal)}"
        tvComision.text = "Comisión de entrega: $${String.format("%.2f", comision)}"
        tvTotal.text = "TOTAL A PAGAR: $${String.format("%.2f", total)}"

        // CONECTAMOS EL RECYCLERVIEW
        rvItems.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CartAdapter(itemsDelCarrito) // ¡Le pasamos los items!
        rvItems.adapter = adapter // ¡Pum!

        // ¡El de Agregar más
        btnAgregarMas.setOnClickListener {
            dismiss() // Cierra el modal (Y como 'Details' ya se quedó vivo, ¡regresa al Home!)
        }

        // El de "CONFIRMAR
        btnConfirmar.setOnClickListener {


            Toast.makeText(requireContext(), "¡Pedido confirmado! Aún no jala JAJJA", Toast.LENGTH_SHORT).show()
            CartManager.clearCart() //
            dismiss() //
        }
    }
}