package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // ¡¡IMPORT!!
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import host.senk.foodtec.R
import host.senk.foodtec.model.CartItem



class CartAdapter(
    private val listaDeItems: List<CartItem>,
    private val onRemoveClicked: (CartItem) -> Unit //
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    // ¡La "cajita" con los botones nuevos!
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivCartImagen)
        val tvCantidad: TextView = view.findViewById(R.id.tvCartCantidad) // ¡El "x1"
        val tvNombre: TextView = view.findViewById(R.id.tvCartNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvCartPrecio)
        val btnRemove: ImageButton = view.findViewById(R.id.btnCartRemove) // El bote
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaDeItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaDeItems[position]

        // AQUÍ PINTAMOS EL CONTADOR
        holder.tvCantidad.text = "x${item.cantidad}"

        holder.tvNombre.text = item.nombre

        // Calculamos el precio total (Precio * Cantidad)
        val precioTotalItem = (item.precio_unitario.toDoubleOrNull() ?: 0.0) * item.cantidad
        holder.tvPrecio.text = "$${String.format("%.2f", precioTotalItem)}"

        Glide.with(holder.itemView.context)
            .load(item.imagen_url)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.ivImagen)

        //LE DAMOS JALE AL BOTE DE BASURA
        holder.btnRemove.setOnClickListener {
            onRemoveClicked(item) // ¡Llama al "oído"!
        }
    }
}