package host.senk.foodtec.ui

import android.os.Bundle
import android.widget.TextView // ¡Import pa'l Texto!
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import host.senk.foodtec.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        //pal saludo
        val tvBienvenido: TextView = findViewById(R.id.tvBienvenido)

        val nombreUsuario = intent.getStringExtra("NOMBRE_USUARIO") ?: "Usuario"

        tvBienvenido.text = "Bienvenido, $nombreUsuario"


    }
}