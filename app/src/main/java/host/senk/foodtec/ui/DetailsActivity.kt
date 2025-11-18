package host.senk.foodtec.ui
import android.content.Intent
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
import host.senk.foodtec.manager.SessionManager


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
                if (SessionManager.getHasActiveOrder(this)) {

                    // 1. Si SÍ tiene pedido, le avisamos
                    Toast.makeText(this, "¡Ya tienes un pedido en curso!", Toast.LENGTH_LONG).show()

                    // Lo mandamos a la pantalla de Pedidos
                    val intent = Intent(this, PedidosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {

                    // 3. Si NO tiene pedido, jala la lógica de siempre
                    val extras = etExtras.text.toString().trim()
                    CartManager.addItem(comida!!, extras) // ¡Ojo con el '!!' si hiciste el ComidaItem nulable!
                    Toast.makeText(this, "¡${comida!!.nombre} agregado al pedido!", Toast.LENGTH_SHORT).show()
                    val modal = CartModalFragment()
                    modal.show(supportFragmentManager, "MODAL_CARRITO")
                }

            }


        } else {
            // si no llega
            Toast.makeText(this, "Error: No se pudo cargar el producto.", Toast.LENGTH_LONG).show()
            finish() // ¡Nos regresamos al Home!
        }
    }
}