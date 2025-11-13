package host.senk.foodtec.ui

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import host.senk.foodtec.R
import host.senk.foodtec.model.ComidaItem

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        val ivImagen: ImageView = findViewById(R.id.ivDetalleImagen)
        val tvNombre: TextView = findViewById(R.id.tvDetalleNombre)
        val tvPrecio: TextView = findViewById(R.id.tvDetallePrecio)
        val rbValoracion: RatingBar = findViewById(R.id.rbDetalleValoracion)
        val tvDescripcion: TextView = findViewById(R.id.tvDetalleDescripcion)
        val etExtras: EditText = findViewById(R.id.etDetalleExtras)
        val btnAgregar: Button = findViewById(R.id.btnAgregarPedido)

       // Sacamos el ComidaItem que nos mandó HomeActivity
        val comida: ComidaItem? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //  jala un Parcelable!
            intent.getParcelableExtra("COMIDA_SELECCIONADA", ComidaItem::class.java)
        } else {
            // jala pa' celulares viejos!
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("COMIDA_SELECCIONADA")
        }


        if (comida != null) {


            // Pintamos los textos
            tvNombre.text = comida.nombre
            tvPrecio.text = "$${comida.precio}"
            tvDescripcion.text = comida.descripcion

            // Convertimos el String a Float pára valorar
            rbValoracion.rating = comida.valoracion.toFloatOrNull() ?: 0f

            // glide para la fotp
            Glide.with(this)
                .load(comida.imagen_url)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(ivImagen)

            // Por ahora, un Toast
            btnAgregar.setOnClickListener {
                val extras = etExtras.text.toString().trim()
                Toast.makeText(this, "¡Agregando ${comida.nombre} con extras: $extras!", Toast.LENGTH_LONG).show()

                // CARRITOS
            }

        } else {

            Toast.makeText(this, "Error: No se pudo cargar el platillo.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}