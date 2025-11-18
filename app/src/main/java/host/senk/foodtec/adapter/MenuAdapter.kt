package host.senk.foodtec.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout // ¡El 'import' que ya tenías!
import android.widget.TextView
import androidx.cardview.widget.CardView // ¡¡IMPORT PA'L CARDVIEW!!
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import host.senk.foodtec.R
import host.senk.foodtec.model.ComidaItem
import android.content.Intent
import android.widget.Toast
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.ui.PedidosActivity

class MenuAdapter(
    private val listaDeComida: List<ComidaItem>,
    private val onItemClicked: (ComidaItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    /**
     * El "ViewHolder": ¡Ahora amarra el CardView Y el LinearLayout!
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ¡El 'itemView' ES el CardView!
        val itemContainer: CardView = itemView as CardView

        // ¡Buscamos al "hijo" (el layout de adentro)!
        val contentLayout: LinearLayout = view.findViewById(R.id.item_content_layout)

        // (Estos ya los tenías)
        val ivImagen: ImageView = view.findViewById(R.id.ivComidaImagen)
        val tvNombre: TextView = view.findViewById(R.id.tvComidaNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvComidaPrecio)
    }

    /**
     * "onCreateViewHolder" (Este jala igual)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comida, parent, false)
        return ViewHolder(view)
    }

    /**
     * "getItemCount" (Este jala igual)
     */
    override fun getItemCount(): Int {
        return listaDeComida.size
    }

    /**
     * "onBindViewHolder": ¡AQUÍ ESTÁ EL JALE CHIDO!
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaDeComida[position]
        holder.tvNombre.text = item.nombre
        holder.tvPrecio.text = "$${item.precio}"

        // (Tu código de Glide queda igual)
        Glide.with(holder.itemView.context)
            .load(item.imagen_url)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.ivImagen)

        // (Tu código de los bordes de color queda igual)
        if (item.categoria == "Bebida") {
            holder.contentLayout.setBackgroundResource(R.drawable.border_outline_bebida)
        } else {
            holder.contentLayout.setBackgroundResource(R.drawable.border_outline_comida)
        }

        // --- ¡¡AQUÍ ESTÁ LA LÓGICA DEL CANDADO!! ---
        holder.itemContainer.setOnClickListener {
            // 1. Sacamos el "context" (la Activity)
            val context = holder.itemView.context

            // 2. ¡Revisamos el "archivero"!
            if (SessionManager.getHasActiveOrder(context)) {

                // 3. SI SÍ TIENE: ¡Pa'trás, compa!
                Toast.makeText(context, "¡Ya tienes un pedido en curso!", Toast.LENGTH_LONG).show()

                // 4. Lo mandamos a la pantalla de Pedidos
                val intent = Intent(context, PedidosActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)

            } else {

                // 5. SI NO TIENE: ¡Pásele! (La lógica de siempre)
                onItemClicked(item)
            }
        }
    }
}