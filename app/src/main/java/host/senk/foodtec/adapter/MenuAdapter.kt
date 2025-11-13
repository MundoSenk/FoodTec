package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import host.senk.foodtec.R
import host.senk.foodtec.model.ComidaItem
import android.util.Log

// --- ¡¡IMPORT #1!! ¡AQUÍ FALTABA ESTE, PA! ---
import android.widget.LinearLayout


class MenuAdapter(
    private val listaDeComida: List<ComidaItem>,
    private val onItemClicked: (ComidaItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    /**
     * El "ViewHolder": Es la cajitaque amarra los IDs del XML
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val itemContainer: LinearLayout = itemView as LinearLayout
        val ivImagen: ImageView = view.findViewById(R.id.ivComidaImagen)
        val tvNombre: TextView = view.findViewById(R.id.tvComidaNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvComidaPrecio)
    }

    /**
     * "onCreateViewHolder": Aquí creamos el molde
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comida, parent, false)
        return ViewHolder(view)
    }

    /**
     * "getItemCount": La más fácil Cuántos hady?
     */
    override fun getItemCount(): Int {
        return listaDeComida.size
    }

    /**

     * Este se llama por cada item.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //  Agarramos el platillo que toca
        val item = listaDeComida[position]

        // Le metemos los datos
        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = "$${item.precio}"

        // Jala la foto
        Glide.with(holder.itemView.context)
            .load(item.imagen_url)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo) // ¡Los michis! Jaja
            .into(holder.ivImagen)

        Log.d("CATEGORIA_DEBUG", "Item: ${item.nombre}, Categoria: '${item.categoria}'")

        //  Le ponemos el fondo según la categoría
        if (item.categoria == "Bebida") {
            // fondo azul
            holder.itemContainer.setBackgroundResource(R.drawable.shape_item_bebida)
        } else {
            // fondo naranja
            holder.itemContainer.setBackgroundResource(R.drawable.shape_item_comida)
        }



        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }
}