package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.model.Pedido

class PedidosDisponiblesAdapter(
    private val listaDePedidos: List<Pedido>,
    private val onAceptarClicked: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidosDisponiblesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPedidoId: TextView = view.findViewById(R.id.tvFoodterPedidoId)
        val tvCliente: TextView = view.findViewById(R.id.tvFoodterCliente)
        val rbValoracion: RatingBar = view.findViewById(R.id.rbValoracionCliente)
        val tvLugar: TextView = view.findViewById(R.id.tvFoodterLugar)
        val tvMetodo: TextView = view.findViewById(R.id.tvFoodterMetodo)
        val tvResumen: TextView = view.findViewById(R.id.tvFoodterResumen)
        val tvTotal: TextView = view.findViewById(R.id.tvFoodterTotal)
        val btnAceptar: Button = view.findViewById(R.id.btnAceptarPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_disponible, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaDePedidos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido = listaDePedidos[position]

        holder.tvPedidoId.text = "Pedido: #${pedido.id_pedido}"
        holder.tvCliente.text = "Para: ${pedido.nombre_cliente ?: "N/A"}"
        holder.tvLugar.text = "Lugar: ${pedido.lugar_entrega ?: "N/A"}"
        holder.tvTotal.text = "$${pedido.costo_final ?: "0.00"}"
        holder.tvMetodo.text = "Pago: ${pedido.metodo_pago ?: "N/A"}"

        // AQUÍ ESTÁ EL PARSEO, JEFE

        // Intenta "parsear" (convertir) el String? ("0.0") a un Double? (0.0)
        val valoracionNumerica = pedido.valoracion_cliente?.toDoubleOrNull()

        //  Si el Double? es 'null', usa 3.0
        holder.rbValoracion.rating = (valoracionNumerica ?: 3.0).toFloat()



        // (El resto de tu código del resumen queda igual)
        val resumen = pedido.detalles
            ?.filterNotNull()
            ?.joinToString(", ") { detalle ->
                "${detalle.cantidad ?: 0}x ${detalle.nombre ?: "?"}"
            } ?: "Sin detalles"

        if (resumen.isEmpty()) {
            holder.tvResumen.text = "Error al cargar detalles"
        } else {
            holder.tvResumen.text = resumen
        }

        holder.btnAceptar.setOnClickListener {
            onAceptarClicked(pedido)
        }
    }
}