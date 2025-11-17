package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.model.Pedido // ¡El molde "gordo"!

class PedidosDisponiblesAdapter(
    private val listaDePedidos: List<Pedido>,
    // EL "OÍDO" PA'L BOTÓN DE ACEPTAR
    private val onAceptarClicked: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidosDisponiblesAdapter.ViewHolder>() {

    // Amarramos" las vistas del item_pedido_disponible
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

        // ¡Pintamos los datos!
        holder.tvPedidoId.text = "Pedido: #${pedido.id_pedido}"
        holder.tvCliente.text = "Para: ${pedido.nombre_cliente ?: "N/A"}"
        holder.tvLugar.text = "Lugar: ${pedido.lugar_entrega ?: "N/A"}"
        holder.tvTotal.text = "$${pedido.costo_final ?: "0.00"}"
        holder.rbValoracion.rating = (pedido.valoracion_cliente ?: 3.0).toFloat()
        holder.tvMetodo.text = "Pago: ${pedido.metodo_pago ?: "N/A"}"

        // Usamos 'filterNotNull Y los nombres nuevos!
        val resumen = pedido.detalles
            ?.filterNotNull() // Filtramos los "fantasmas" (por si acaso)
            ?.joinToString(", ") { detalle ->
                // Usamos 'detalle.nombre' en lugar de detalle.nombre_alimento
                "${detalle.cantidad ?: 0}x ${detalle.nombre ?: "?"}"
            } ?: "Sin detalles"

        //Validamos que el resumen no esté vacío
        if (resumen.isEmpty()) {
            holder.tvResumen.text = "Error al cargar detalles"
        } else {
            holder.tvResumen.text = resumen
        }

        // Alambramos" el botón de Aceptar
        holder.btnAceptar.setOnClickListener {
            onAceptarClicked(pedido)
        }
    }

}
