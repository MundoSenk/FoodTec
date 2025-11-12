package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.model.ComidaItem

//El Adapter recibe la lista de platillos
class MenuAdapter(private val listaDeComida: List<ComidaItem>) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivComidaImagen)
        val tvNombre: TextView = view.findViewById(R.id.tvComidaNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvComidaPrecio)
    }

    /**
        * Le decimos a Android: "Oye, agarra el 'item_comida.xml' y
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comida, parent, false)
        return ViewHolder(view)
    }

    /**
     * Android pregunta: "¿Cuántos platillos hay en la lista?"
     * Y nosotros le decimos: "Pues los que traiga la listaDeComida".
     */
    override fun getItemCount(): Int {
        return listaDeComida.size
    }

    /**
     * onBindViewHolder
     * Este se llama por cada item. Android nos dice:
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Agarramos el platillo de la lista que toca (ej. "Hamburguesa")
        val item = listaDeComida[position]


        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = "$${item.precio}" // Le ponemos el signito de pesos

    }
}