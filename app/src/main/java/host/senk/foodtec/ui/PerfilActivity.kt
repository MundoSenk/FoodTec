package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log //
import android.view.View
import android.widget.Button
import android.widget.ImageView //
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import host.senk.foodtec.R
import host.senk.foodtec.model.AvatarRequest
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.CrearPedidoResponse
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

    // NUEVAS VISTAS
    private lateinit var ivAvatar: CircleImageView
    private lateinit var btnCambiarAvatar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Amarramos Vistas
        bindViews()

        // Pintamos" los datos (¡Ahora también el avatar!)
        pintarDatos()

        // Alambramos el Nav Bar
        setupBottomNav()

        // Alambramos" el botón de Logout (queda igual)
        btnCerrarSesion.setOnClickListener {
            SessionManager.logout(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // NUEVO LISTENER
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

        // --- ¡NUEVOS BINDS! ---
        ivAvatar = findViewById(R.id.ivAvatar)
        btnCambiarAvatar = findViewById(R.id.btnCambiarAvatar)
    }

    private fun pintarDatos() {
        // Jalamos los datos del "archivero"
        val nombre = SessionManager.getUserName(this)
        val esFoodter = SessionManager.isFoodter(this)
        val valCliente = SessionManager.getValoracionCliente(this)
        val valFoodter = SessionManager.getValoracionFoodter(this)
        val avatarId = SessionManager.getAvatarId(this) // <-- ¡¡NUEVO DATO!!

        // ¡Pintamos!
        tvTitulo.text = "Perfil de: ${nombre ?: "Usuario"}"
        rbCliente.rating = valCliente

        // PINTAMOS EL AVATAR
        ivAvatar.setImageResource(getAvatarResource(avatarId))

        // Si es Foodter, mostramos su valoración
        if (esFoodter) {
            llFoodter.visibility = View.VISIBLE
            rbFoodter.rating = valFoodter
        } else {
            llFoodter.visibility = View.GONE
        }
    }

    /**
     * ¡NUEVA FUNCIÓN "TRADUCTORA"!
     * Convierte el "avatar_1" (String) en R.drawable.avatar_1 (Int)
     */
    private fun getAvatarResource(avatarId: String): Int {
        return when (avatarId) {
            "avatar_1" -> R.drawable.avatar_1
            "avatar_2" -> R.drawable.avatar_2
            "avatar_3" -> R.drawable.avatar_3
            // (Si agregas más, ponlos aquí)
            else -> R.drawable.avatar_default // ¡Recuerda tener avatar_default.png!
        }
    }

    /**
     * ¡NUEVA FUNCIÓN! Muestra el diálogo de selección.
     */
    private fun mostrarDialogoAvatares() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        // Inflamos el XML que creaste
        val dialogView = inflater.inflate(R.layout.dialog_seleccionar_avatar, null)
        builder.setView(dialogView)
        builder.setTitle("Selecciona un Avatar")

        val dialog = builder.create()

        // Amarramos las imágenes del DIÁLOGO
        val img1: ImageView = dialogView.findViewById(R.id.select_avatar_1)
        val img2: ImageView = dialogView.findViewById(R.id.select_avatar_2)
        val img3: ImageView = dialogView.findViewById(R.id.select_avatar_3)

        // Asignamos los listeners
        img1.setOnClickListener {
            llamarApiActualizarAvatar("avatar_1", dialog)
        }
        img2.setOnClickListener {
            llamarApiActualizarAvatar("avatar_2", dialog)
        }
        img3.setOnClickListener {
            llamarApiActualizarAvatar("avatar_3", dialog)
        }

        dialog.show()
    }

    /**
     * ¡NUEVA FUNCIÓN! Esta es la que llama a Retrofit.
     */
    private fun llamarApiActualizarAvatar(nuevoAvatarId: String, dialog: AlertDialog) {

        // ¡Jalamos el ID de usuario ("RENO") del "archivero"!
        val usuarioId = SessionManager.getUserId(this)
        if (usuarioId == null) {
            Toast.makeText(this, "Error fatal: No se encontró tu usuario.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("AVATAR_API", "Intentando actualizar: Usuario $usuarioId, Avatar $nuevoAvatarId")

        // Creamos el "molde" del Request
        val request = AvatarRequest(usuario_id = usuarioId, avatar_id = nuevoAvatarId)

        //  Llamamos a Retrofit
        RetrofitClient.apiService.actualizarAvatar(request).enqueue(object : Callback<CrearPedidoResponse> {

            override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val respuesta = response.body()!!

                    if (respuesta.status == "exito") {
                        // ÉXITO
                        Log.d("AVATAR_API", "¡Éxito! Avatar actualizado en BD.")

                        // Guardamos el nuevo avatar en el "archivero"
                        SessionManager.setAvatarId(this@PerfilActivity, nuevoAvatarId)

                        // Actualizamos la imagen en la pantalla (sin recargar)
                        ivAvatar.setImageResource(getAvatarResource(nuevoAvatarId))

                        // Mostramos un Toast
                        Toast.makeText(this@PerfilActivity, "¡Avatar actualizado!", Toast.LENGTH_SHORT).show()

                        // Cerramos el diálogo
                        dialog.dismiss()

                    } else {
                        // El PHP dijo "error"
                        Log.e("AVATAR_API", "Error del PHP: ${respuesta.mensaje}")
                        Toast.makeText(this@PerfilActivity, "Error: ${respuesta.mensaje}", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                } else {
                    // El server respondió 404, 500, etc.
                    Log.e("AVATAR_API", "Error de servidor: ${response.code()}")
                    Toast.makeText(this@PerfilActivity, "Error del servidor (${response.code()})", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                // No hay net o Retrofit se rompió
                Log.e("AVATAR_API", "Falla de Retrofit: ${t.message}", t)
                Toast.makeText(this@PerfilActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }


    // (setupBottomNav() queda igual)
    private fun setupBottomNav() {
        bottomNavView.selectedItemId = R.id.nav_perfil

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_pedidos -> {
                    val intent = Intent(this, PedidosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> true
                else -> false
            }
        }
    }
}