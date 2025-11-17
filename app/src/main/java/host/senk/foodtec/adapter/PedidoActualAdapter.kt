package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ¡¡OJO: Necesitamos Glide!!
import host.senk.foodtec.R
import host.senk.foodtec.model.PedidoDetalle // ¡El "sub-molde"!

class PedidoActualAdapter(
    private val listaDeDetalles: List<PedidoDetalle>
) : RecyclerView.Adapter<PedidoActualAdapter.ViewHolder>() {

    // El "ViewHolder" amarra las vistas del item_pedido_actual.xml
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivItemImagen)
        val tvNombre: TextView = view.findViewById(R.id.tvItemNombre)
        val tvCantidad: TextView = view.findViewById(R.id.tvItemCantidad)
        val tvDetallesUser: TextView = view.findViewById(R.id.tvItemDetallesUser)
        val tvPrecioTotal: TextView = view.findViewById(R.id.tvItemPrecioTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflamos (creamos) el XML del item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido_actual, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        // El tamaño de la lista de detalles
        return listaDeDetalles.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Sacamos el detalle de esta posición
        val detalle = listaDeDetalles[position]

        // --- ¡¡LÓGICA ANTIBALA!! ---

        // Calculamos el precio (¡ahora con null checks!)
        val precioUnit = detalle.precio_unitario?.toDoubleOrNull() ?: 0.0
        val cantidad = detalle.cantidad ?: 0 // Si la cantidad es null, asume 0
        val precioTotalRenglon = precioUnit * cantidad

        // ¡Pintamos los datos (con null checks!)
        // Si el nombre es nulo, ponemos un placeholder
        holder.tvNombre.text = detalle.nombre_alimento ?: "Producto no disponible"
        holder.tvCantidad.text = "Cantidad: x${cantidad}"
        holder.tvPrecioTotal.text = "$${String.format("%.2f", precioTotalRenglon)}"

        // Mostramos los "detalles del usuario" (esta lógica ya era segura)
        if (!detalle.detalles_usuario.isNullOrEmpty()) {
            holder.tvDetallesUser.visibility = View.VISIBLE
            holder.tvDetallesUser.text = detalle.detalles_usuario
        } else {
            holder.tvDetallesUser.visibility = View.GONE
        }

        // ¡Usamos Glide (con null check!)
        Glide.with(holder.itemView.context)
            .load(detalle.imagen ?: "") // Si la imagen es null, carga un string vacío (mostrará el 'error')
            .placeholder(R.drawable.logo) // Imagen mientras carga
            .error(R.drawable.logo) // Imagen si truena
            .into(holder.ivImagen)
    }
}