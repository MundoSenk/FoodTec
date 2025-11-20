package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.model.PedidoDetalle

class ValoracionItemsAdapter(
    private val items: List<PedidoDetalle>
) : RecyclerView.Adapter<ValoracionItemsAdapter.ViewHolder>() {

    // Mapa para guardar: Posición en la lista -> Calificación
    val calificaciones = mutableMapOf<Int, Float>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProductoValorar)
        val rbValoracion: RatingBar = view.findViewById(R.id.rbValorarProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_valorar_producto, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        // Si viene nulo el nombre, ponemos un default
        holder.tvNombre.text = item.nombre ?: "Producto"

        // Quitamos el listener antes de cambiar el valor para evitar ciclos infinitos
        holder.rbValoracion.setOnRatingBarChangeListener(null)

        // Recuperamos la calificación si ya la habíamos guardado, si no 0
        holder.rbValoracion.rating = calificaciones[position] ?: 0f

        // Ponemos el listener para guardar cuando el usuario toque la estrella
        holder.rbValoracion.setOnRatingBarChangeListener { _, rating, _ ->
            calificaciones[position] = rating
        }
    }
}