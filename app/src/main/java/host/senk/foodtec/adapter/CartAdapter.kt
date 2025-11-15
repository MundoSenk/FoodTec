package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import host.senk.foodtec.R
import host.senk.foodtec.model.CartItem // Usa el 'CartItem'

// ¡Este jala con la lista del CartManager'
class CartAdapter(private val listaDeItems: List<CartItem>) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    // xml
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivCartImagen)
        val tvNombre: TextView = view.findViewById(R.id.tvCartNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvCartPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // llena item_cart.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaDeItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //  'CartItem' que le toca
        val item = listaDeItems[position]

        // Llenamo item_cart.xml
        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = "$${item.precio_unitario}"

        // Glide pa' la foto
        Glide.with(holder.itemView.context)
            .load(item.imagen_url)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.ivImagen)
    }
}