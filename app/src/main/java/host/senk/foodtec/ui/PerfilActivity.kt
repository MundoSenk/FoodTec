package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import host.senk.foodtec.R
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.AvatarRequest
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.EstadisticasResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilActivity : AppCompatActivity() {

    // --- Vistas ---
    private lateinit var tvTitulo: TextView
    private lateinit var rbCliente: RatingBar
    private lateinit var llFoodter: LinearLayout
    private lateinit var rbFoodter: RatingBar
    private lateinit var btnCerrarSesion: Button
    private lateinit var bottomNavView: BottomNavigationView

    // Avatar
    private lateinit var ivAvatar: CircleImageView
    private lateinit var btnCambiarAvatar: Button

    // Variables para Gamificación
    private var misPedidosCompletados: Int = 0
    private var soyUsuarioGod: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        bindViews()
        pintarDatos()
        setupBottomNav()

        // Cargamos estadísticas en segundo plano al entrar
        cargarEstadisticas()

        // Botón Logout
        btnCerrarSesion.setOnClickListener {
            SessionManager.logout(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Botón Cambiar Avatar
        btnCambiarAvatar.setOnClickListener {
            mostrarDialogoAvatares()
        }
    }

    private fun bindViews() {
        tvTitulo = findViewById(R.id.tvTituloPerfil)
        rbCliente = findViewById(R.id.rbValoracionCliente)
        llFoodter = findViewById(R.id.llValoracionFoodter)
        rbFoodter = findViewById(R.id.rbValoracionFoodter)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        bottomNavView = findViewById(R.id.bottomNavViewPerfil)
        ivAvatar = findViewById(R.id.ivAvatar)
        btnCambiarAvatar = findViewById(R.id.btnCambiarAvatar)
    }

    private fun pintarDatos() {
        val nombre = SessionManager.getUserName(this)
        val esFoodter = SessionManager.isFoodter(this)
        val valCliente = SessionManager.getValoracionCliente(this)
        val valFoodter = SessionManager.getValoracionFoodter(this)
        val avatarId = SessionManager.getAvatarId(this)

        tvTitulo.text = "Perfil de: ${nombre ?: "Usuario"}"
        rbCliente.rating = valCliente

        ivAvatar.setImageResource(getAvatarResource(avatarId))

        if (esFoodter) {
            llFoodter.visibility = View.VISIBLE
            rbFoodter.rating = valFoodter
        } else {
            llFoodter.visibility = View.GONE
        }
    }

    private fun cargarEstadisticas() {
        val userId = SessionManager.getUserId(this) ?: return


        RetrofitClient.apiService.obtenerEstadisticas(userId).enqueue(object : Callback<EstadisticasResponse> {
            override fun onResponse(call: Call<EstadisticasResponse>, response: Response<EstadisticasResponse>) {
                if (response.isSuccessful && response.body()?.status == "exito") {
                    val stats = response.body()!!
                    misPedidosCompletados = stats.pedidos_como_cliente
                    soyUsuarioGod = stats.es_usuario_god // <--- ¡GUARDAMOS EL PODER!
                }
            }
            override fun onFailure(call: Call<EstadisticasResponse>, t: Throwable) {}
        })
    }

    private fun getAvatarResource(avatarId: String): Int {
        return when (avatarId) {
            "avatar_1" -> R.drawable.avatar_1
            "avatar_2" -> R.drawable.avatar_2
            "avatar_3" -> R.drawable.avatar_3
            "avatar_4" -> R.drawable.avatar_4
            "avatar_5" -> R.drawable.avatar_5
            "avatar_6" -> R.drawable.avatar_6
            "avatar_7" -> R.drawable.avatar_7
            "avatar_8" -> R.drawable.avatar_8
            "avatar_9" -> R.drawable.avatar_9
            "avatar_oculto" -> R.drawable.avatar_oculto
            else -> R.drawable.avatar_defaut
        }
    }

    private fun mostrarDialogoAvatares() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_seleccionar_avatar, null)

        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancelar = dialogView.findViewById<View>(R.id.btnCancelarAvatar)
        btnCancelar.setOnClickListener { dialog.dismiss() }

        // DATOS PARA LA LÓGICA
        val miRating = SessionManager.getValoracionCliente(this) // Estrellas
        val misPedidos = misPedidosCompletados // Cantidad

        // LISTA DE IMAGENES
        val avatars = listOf(
            dialogView.findViewById<ImageView>(R.id.select_avatar_1),
            dialogView.findViewById<ImageView>(R.id.select_avatar_2),
            dialogView.findViewById<ImageView>(R.id.select_avatar_3),
            // GRUPO ESTRELLAS
            dialogView.findViewById<ImageView>(R.id.select_avatar_4),
            dialogView.findViewById<ImageView>(R.id.select_avatar_5),
            dialogView.findViewById<ImageView>(R.id.select_avatar_6),
            //  GRUPO PEDIDOS
            dialogView.findViewById<ImageView>(R.id.select_avatar_7),
            dialogView.findViewById<ImageView>(R.id.select_avatar_8),
            dialogView.findViewById<ImageView>(R.id.select_avatar_9)
        )

        val imgGod = dialogView.findViewById<ImageView>(R.id.select_avatar_god)
        //You are god?
        if (soyUsuarioGod) {
            // ES DIGNO
            imgGod.visibility = View.VISIBLE
            imgGod.setOnClickListener {
                llamarApiActualizarAvatar("avatar_oculto", dialog)
            }
        } else {
            // MERO MORTAL.
            imgGod.visibility = View.GONE
        }

        avatars.forEachIndexed { index, imageView ->
            val numAvatar = index + 1
            val idString = "avatar_$numAvatar"

            var bloqueado = false
            var mensajeBloqueo = ""

            // --- LÓGICA HÍBRIDA ---
            if (index in 0..2) {
                bloqueado = false
            }
            else if (index in 3..5) {
                if (miRating < 4.0) {
                    bloqueado = true
                    mensajeBloqueo = "Requiere 4.0 estrellas de reputación"
                }
            }
            else if (index in 6..8) {
                if (misPedidos < 50) {
                    bloqueado = true
                    mensajeBloqueo = "Requiere haber completado 50 pedidos (Llevas $misPedidos)"
                }
            }

            // APLICAR LÓGICA
            if (bloqueado) {
                imageView.alpha = 0.3f
                imageView.setOnClickListener {
                    Toast.makeText(this, mensajeBloqueo, Toast.LENGTH_SHORT).show()
                }
            } else {
                imageView.alpha = 1.0f
                imageView.setOnClickListener {
                    llamarApiActualizarAvatar(idString, dialog)
                }
            }
        }

        dialog.show()
    }

    private fun llamarApiActualizarAvatar(nuevoAvatarId: String, dialog: AlertDialog) {
        val usuarioId = SessionManager.getUserId(this) ?: return
        val request = AvatarRequest(usuario_id = usuarioId, avatar_id = nuevoAvatarId)

        RetrofitClient.apiService.actualizarAvatar(request).enqueue(object : Callback<CrearPedidoResponse> {
            override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                if (response.isSuccessful && response.body()?.status == "exito") {
                    SessionManager.setAvatarId(this@PerfilActivity, nuevoAvatarId)
                    ivAvatar.setImageResource(getAvatarResource(nuevoAvatarId))
                    Toast.makeText(this@PerfilActivity, "¡Avatar actualizado!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@PerfilActivity, response.body()?.mensaje ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                Toast.makeText(this@PerfilActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomNav() {
        bottomNavView.selectedItemId = R.id.nav_perfil
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.nav_pedidos -> {
                    startActivity(Intent(this, PedidosActivity::class.java))
                    true
                }
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }
}