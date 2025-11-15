package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import host.senk.foodtec.model.LoginResponse
import host.senk.foodtec.R
import host.senk.foodtec.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TabLayout
import com.google.android.material.tabs.TabLayout

//Pa que jale el scroll
import android.widget.ScrollView
import android.view.View

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  XML
        val etUsuario: EditText = findViewById(R.id.etUsuario)
        val etContra: EditText = findViewById(R.id.etContrasena)
        val btnLogin: Button = findViewById(R.id.btnIniciarSesion)
        val tvRegistrate: TextView = findViewById(R.id.txRegistrateAqui) // El link viejo
        val tabLayout: TabLayout = findViewById(R.id.tabLayout) // El toggle chido
        val scrollView: ScrollView = findViewById(R.id.scrollView) // ¡El Scroll!

        // NUEVO: Enlace "¿Olvidaste tu contraseña?"
        val tvOlvideContrasena: TextView = findViewById(R.id.tvOlvideContrasena)

        //
        // Este es el oído pa'l scroll
        etContra.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // pa' mostrar el botón de "Iniciar Sesion"
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        // NUEVO: Lógica para el clic en "¿Olvidaste tu contraseña?"
        tvOlvideContrasena.setOnClickListener {
            // Navega a la Activity de restablecimiento de contraseña
            val intent = Intent(this, PasswordResetActivity::class.java)
            startActivity(intent)
        }

        ///tablayoutttt
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Cuando el compa le pica a una pestaña
                if (tab?.position == 1) {
                    // Si le picó a Registrarse
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        btnLogin.setOnClickListener {
            // Jalamos el texto que puso el vato
            val usuario = etUsuario.text.toString().trim()
            val contra = etContra.text.toString()

            // Checamos si no se pasó de listo y dejó todo vacío
            if (usuario.isEmpty() || contra.isEmpty()) {
                Toast.makeText(this, "Ingresa usuario y contraseña, pa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // mandar al cartero (Retrofit)
            val call = RetrofitClient.apiService.loginUsuario(usuario, contra)

            // Lo formamos pa' que no congele la app
            call.enqueue(object : Callback<LoginResponse> {

                // SI EL CARTERO SÍ LLEGÓ
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginRespuesta = response.body()!!

                        // ¡Leemos qué dijo el PHP!
                        if (loginRespuesta.status == "exito") {
                            Toast.makeText(this@LoginActivity, loginRespuesta.mensaje, Toast.LENGTH_LONG).show()

                            // ¡Nos vamos al Home!
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("NOMBRE_USUARIO", loginRespuesta.nombre)
                            intent.putExtra("USER_NAME", loginRespuesta.usuario)
                            startActivity(intent)
                            finish()

                        } else if (loginRespuesta.status == "error_verificacion") {

                            Toast.makeText(this@LoginActivity, loginRespuesta.mensaje, Toast.LENGTH_LONG).show()

                            // Lo mandamos a la pantalla de Verificar
                            val intent = Intent(this@LoginActivity, VerifyActivity::class.java)
                            // Le pasamos el correo que el PHP nos regresó
                            intent.putExtra("CORREO_USUARIO", loginRespuesta.correo)
                            startActivity(intent)

                        } else {
                            // Si nos trono por ootra cosa (pass incorrecto, etc.)
                            Toast.makeText(this@LoginActivity, loginRespuesta.mensaje, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Si el servidor tronó (Error 500, 404)
                        Toast.makeText(this@LoginActivity, "Error del server: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e("API_ERROR_LOGIN", "El server se fue a mimir: ${response.errorBody()?.string()}")
                    }
                }

                // SI EL CARTERO NI LLEGÓ
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "No hay net, pa: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("NETWORK_ERROR_LOGIN", "Falló Retrofit", t)
                }
            })
        }


        tvRegistrate.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}