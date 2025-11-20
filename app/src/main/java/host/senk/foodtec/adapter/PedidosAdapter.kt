package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.model.Pedido

class PedidosAdapter(
    private val listaDePedidos: List<Pedido>,
    private val onItemClicked: (Pedido) -> Unit // La función lambda que recibe el clic
) : RecyclerView.Adapter<PedidosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvHistorialId)
        val tvResumen: TextView = view.findViewById(R.id.tvHistorialResumen)
        val rbValoracion: RatingBar = view.findViewById(R.id.rbHistorialValoracion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_anterior, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaDePedidos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido = listaDePedidos[position]

        holder.tvId.text = "#${pedido.id_pedido}"
        holder.tvResumen.text = "(${pedido.estatus})"

        val calificacion = pedido.valoracion_cliente?.toFloatOrNull() ?: 0f
        holder.rbValoracion.rating = calificacion


        holder.itemView.setOnClickListener {


            onItemClicked(pedido)
        }
    }
}