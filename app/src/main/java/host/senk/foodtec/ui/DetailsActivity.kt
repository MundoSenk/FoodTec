package host.senk.foodtec.ui
import host.senk.foodtec.manager.CartManager
import host.senk.foodtec.model.ComidaItem

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



class DetailsActivity : AppCompatActivity() {

    // Jalamos el platillo pa' usarlo en el botón
    private var comida: ComidaItem? = null

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
        comida = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("COMIDA_SELECCIONADA", ComidaItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("COMIDA_SELECCIONADA")
        }

        if (comida != null) {


            // Pintamos los textos
            // ¡Usamos 'comida' (con !!) pa' decirle a Kotlin "¡confía, pa, no es nulo!")
            tvNombre.text = comida!!.nombre
            tvPrecio.text = "$${comida!!.precio}"
            tvDescripcion.text = comida!!.descripcion
            rbValoracion.rating = comida!!.valoracion.toFloatOrNull() ?: 0f

            Glide.with(this)
                .load(comida!!.imagen_url)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(ivImagen)

            // Por ahora, un Toast
            btnAgregar.setOnClickListener {
                //  Jalamos los "detalles" (el "sin salsa")
                val extras = etExtras.text.toString().trim()

                // Usamos el Singleton pa' meter el platillo
                CartManager.addItem(comida!!, extras)

                // Un Toast chido pa' que el vato sepa
                Toast.makeText(this, "¡${comida!!.nombre} agregado al pedido!", Toast.LENGTH_SHORT).show()


                // Creamos el Modal
                val modal = CartModalFragment()


                // Lo mostramos en la pantalla
                modal.show(supportFragmentManager, "MODAL_CARRITO")

            }


        } else {
            // si no llega
            Toast.makeText(this, "Error: No se pudo cargar el producto.", Toast.LENGTH_LONG).show()
            finish() // ¡Nos regresamos al Home!
        }
    }
}