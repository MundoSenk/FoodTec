package host.senk.foodtec

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button // ¡Import!
import android.widget.EditText // ¡Import!
import android.widget.TextView
import android.widget.Toast // ¡Import!
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// ¡¡Imports del Cartero (Retrofit)!!
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // --- Conectamos las vistas del XML ---
        val etUsuario: EditText = findViewById(R.id.etUsuario)
        val etContra: EditText = findViewById(R.id.etContrasena)
        val btnLogin: Button = findViewById(R.id.btnIniciarSesion)
        val tvRegistrate: TextView = findViewById(R.id.txRegistrateAqui) // Tu 'link'

        // --- El "link" para ir a Registrarse (Este ya lo tenías) ---
        tvRegistrate.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // --- ¡¡AQUÍ VA LA CHAMBA DEL BOTÓN DE LOGIN!! ---
        btnLogin.setOnClickListener {

            // 1. Obtenemos los textos
            val usuario = etUsuario.text.toString().trim()
            val contra = etContra.text.toString() // La contra no lleva .trim()

            // 2. Validación básica
            if (usuario.isEmpty() || contra.isEmpty()) {
                Toast.makeText(this, "Ingresa usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. ¡¡A LLAMAR AL CARTERO!!
            val call = RetrofitClient.apiService.loginUsuario(usuario, contra)

            // 4. Poner la llamada en la fila (enqueue)
            call.enqueue(object : Callback<LoginResponse> {

                // SI EL CARTERO LLEGÓ (Hubo respuesta del servidor)
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginRespuesta = response.body()!!

                        // ¡Leemos la respuesta del PHP!
                        if (loginRespuesta.status == "exito") {
                            // ¡ÉXITO! ¡BIENVENIDO!
                            Toast.makeText(this@LoginActivity, loginRespuesta.mensaje, Toast.LENGTH_LONG).show()

                            // ¡AQUÍ IRÍA EL CÓDIGO PARA IR A LA PANTALLA PRINCIPAL!
                            // ej. val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            // intent.putExtra("NOMBRE_USUARIO", loginRespuesta.nombre)
                            // startActivity(intent)
                            // finish() // Cerramos el login para que no pueda volver

                        } else {
                            // Si el PHP nos dijo 'status: "error"' (ej. pass incorrecto)
                            Toast.makeText(this@LoginActivity, loginRespuesta.mensaje, Toast.LENGTH_LONG).show()
                        }

                    } else {
                        // Si el servidor dio error (500, 404, etc.)
                        Toast.makeText(this@LoginActivity, "Error del servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e("API_ERROR_LOGIN", "Respuesta no exitosa: ${response.errorBody()?.string()}")
                    }
                }

                // SI EL CARTERO NO LLEGÓ (No hay internet)
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("NETWORK_ERROR_LOGIN", "Fallo en la llamada Retrofit", t)
                }
            })
        }
    }
}