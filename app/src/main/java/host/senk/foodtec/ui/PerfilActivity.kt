package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import host.senk.foodtec.R
import host.senk.foodtec.manager.SessionManager // ¡¡EL "ARCHIVERO"!!

class PerfilActivity : AppCompatActivity() {

    // --- Vistas (¡¡CON LA SINTAXIS CORRECTA, PA!!) ---
    private lateinit var tvTitulo: TextView
    private lateinit var rbCliente: RatingBar
    private lateinit var llFoodter: LinearLayout
    private lateinit var rbFoodter: RatingBar
    private lateinit var btnCerrarSesion: Button
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Amarramos Vistas
        bindViews()

        // Pintamos" los datos del "Archivero
        pintarDatos()

        // Alambramos el Nav Bar
        setupBottomNav()

        // Alambramos" el botón de Logout
        btnCerrarSesion.setOnClickListener {
            // ¡Llamamos al "archivero" pa' borrar todo!
            SessionManager.logout(this)

            // Nos vamos al Login
            val intent = Intent(this, LoginActivity::class.java)

            // ¡"Matamos" todas las pantallas anteriores!
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Matamos esta también
        }
    }

    private fun bindViews() {
        tvTitulo = findViewById(R.id.tvTituloPerfil)
        rbCliente = findViewById(R.id.rbValoracionCliente)
        llFoodter = findViewById(R.id.llValoracionFoodter)
        rbFoodter = findViewById(R.id.rbValoracionFoodter)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        bottomNavView = findViewById(R.id.bottomNavViewPerfil)
    }

    private fun pintarDatos() {
        // Jalamos los datos del "archivero"
        val nombre = SessionManager.getUserName(this)
        val esFoodter = SessionManager.isFoodter(this)
        val valCliente = SessionManager.getValoracionCliente(this)
        val valFoodter = SessionManager.getValoracionFoodter(this)

        // ¡Pintamos!
        tvTitulo.text = "Perfil de: ${nombre ?: "Usuario"}"
        rbCliente.rating = valCliente


        // Si es Foodter, mostramos su valoración de Foodter
        if (esFoodter) {
            llFoodter.visibility = View.VISIBLE
            rbFoodter.rating = valFoodter
        } else {
            llFoodter.visibility = View.GONE
        }
    }

    private fun setupBottomNav() {
        // Le decimos que "Perfil" está seleccionado
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