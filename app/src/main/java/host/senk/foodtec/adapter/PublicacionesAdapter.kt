package host.senk.foodtec.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import host.senk.foodtec.R
import host.senk.foodtec.model.Publicacion

class PublicacionesAdapter(
    private val lista: List<Publicacion>,
    private val currentUserId: String, //
    private val onBorrarClick: (Publicacion) -> Unit
) : RecyclerView.Adapter<PublicacionesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: CircleImageView = view.findViewById(R.id.ivAvatarPub)
        val tvUsuario: TextView = view.findViewById(R.id.tvUsuarioPub)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaPub)
        val tvTipo: TextView = view.findViewById(R.id.tvTipoPub)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloPub)
        val tvDesc: TextView = view.findViewById(R.id.tvDescPub)
        val ivFoto: ImageView = view.findViewById(R.id.ivFotoPub)
        val btnContactar: Button = view.findViewById(R.id.btnContactar)
        val btnBorrar: android.widget.ImageButton = view.findViewById(R.id.btnBorrarPub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_publicacion, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]


        if (item.usuario_id == currentUserId) {
            holder.btnBorrar.visibility = View.VISIBLE
            holder.btnBorrar.setOnClickListener {
                onBorrarClick(item)
            }
        } else {
            holder.btnBorrar.visibility = View.GONE
        }

        holder.tvUsuario.text = item.nombre_usuario
        holder.tvFecha.text = item.fecha_publicacion // Podrías usar una función "Hace X min"
        holder.tvTitulo.text = item.titulo
        holder.tvDesc.text = item.descripcion
        holder.tvTipo.text = item.tipo.uppercase()

        // Color de la etiqueta según tipo
        if (item.tipo == "Perdido") {
            holder.tvTipo.setBackgroundResource(R.drawable.shape_status_pendiente) // Rojo/Naranja
        } else {
            // Necesitarás un shape azul o verde para "Encontrado", por ahora usa el default azul
        }

        // Avatar
        val avatarRes = when (item.avatar_id) {
            "avatar_1" -> R.drawable.avatar_1
            "avatar_2" -> R.drawable.avatar_2
            "avatar_3" -> R.drawable.avatar_3
            else -> R.drawable.avatar_default
        }
        holder.ivAvatar.setImageResource(avatarRes)

        // Foto del objeto
        Glide.with(holder.itemView.context)
            .load(item.foto_url)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(holder.ivFoto)

        // LOGICA WHATSAPP
        holder.btnContactar.setOnClickListener {
            val context = holder.itemView.context
            val numero = item.contacto_whatsapp
            val mensaje = "Hola! Vi tu publicación en FoodTec sobre: ${item.titulo}"

            val url = "https://api.whatsapp.com/send?phone=$numero&text=$mensaje"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }
    }
}