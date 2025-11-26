package host.senk.foodtec.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.model.Notificacion
import host.senk.foodtec.ui.HomeFoodterActivity
import host.senk.foodtec.ui.ObjetosPerdidosActivity
import host.senk.foodtec.ui.PedidosActivity

class NotificacionesAdapter(
    private val lista: List<Notificacion>
) : RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcono: ImageView = view.findViewById(R.id.ivIconoNotif)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloNotif)
        val tvMensaje: TextView = view.findViewById(R.id.tvMensajeNotif)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaNotif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val context = holder.itemView.context

        holder.tvTitulo.text = item.titulo
        holder.tvMensaje.text = item.mensaje
        holder.tvFecha.text = item.fecha_creacion


        holder.ivIcono.clearColorFilter()
        holder.ivIcono.setPadding(0,0,0,0)

        // Icono según tipo
        when {

            item.tipo.contains("objeto") || item.tipo.contains("alerta_general") -> {
                holder.ivIcono.setImageResource(R.drawable.objetos)

            }

            item.tipo.contains("disponible") -> {
                holder.ivIcono.setImageResource(R.drawable.foodters)
            }

            else -> {
                holder.ivIcono.setImageResource(android.R.drawable.ic_menu_agenda)
                val colorNaranja = androidx.core.content.ContextCompat.getColor(context, R.color.foodtec_naranja)
                holder.ivIcono.setColorFilter(colorNaranja)
                holder.ivIcono.setPadding(8,8,8,8)
            }
        }

        // CLIC INTELIGENTE
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            when (item.tipo) {
                "objeto", "alerta_general" -> {
                    // Vamos a objetos perdidos
                    context.startActivity(Intent(context, ObjetosPerdidosActivity::class.java))
                }
                "pedido_estado", "pedido_aceptado" -> {
                    // Vamos a mis pedidos (Cliente)
                    context.startActivity(Intent(context, PedidosActivity::class.java))
                }
                "pedido_disponible" -> {
                    // Vamos a la pantalla de Foodter (Chamba)
                    context.startActivity(Intent(context, HomeFoodterActivity::class.java))
                }
                else -> {

                    Toast.makeText(context, "Información: ${item.mensaje}", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
}