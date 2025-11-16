package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.model.Pedido // Usa el molde de Pedido!

// ¡Este jala con la lista de obtenerMisPedidos.php!
class PedidosAdapter(
    private val listaDePedidos: List<Pedido>,
    private val onItemClicked: (Pedido) -> Unit // ¡Un "oído" pa'l clic!
) : RecyclerView.Adapter<PedidosAdapter.ViewHolder>() {

    // La "cajita" que amarra los IDs del 'item_pedido_anterior.xml!
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvHistorialId)
        val tvResumen: TextView = view.findViewById(R.id.tvHistorialResumen)
        val rbValoracion: RatingBar = view.findViewById(R.id.rbHistorialValoracion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Infla el item_pedido_anterior.xml que armamos
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_anterior, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaDePedidos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Agarramos el 'Pedido' que toca
        val pedido = listaDePedidos[position]

        // Pintamos
        holder.tvId.text = "#${pedido.id_pedido}"


        // Por ahora, le ponemos el estatus
        holder.tvResumen.text = "(${pedido.estatus})"

        // Aquí jalaríamos la valoración (¡or ahora 0)
        holder.rbValoracion.rating = 0f

        //
        holder.itemView.setOnClickListener {
            onItemClicked(pedido)
        }
    }
}