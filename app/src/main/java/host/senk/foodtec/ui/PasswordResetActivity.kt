package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import host.senk.foodtec.R
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordResetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Encontrar los elementos del XML
        val etEmailRecuperacion: EditText = findViewById(R.id.etEmailRecuperacion)
        val btnRecuperar: Button = findViewById(R.id.btnRecuperar)

        // 2. Configurar el oyente del botón
        btnRecuperar.setOnClickListener {
            val email = etEmailRecuperacion.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu correo electrónico.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Llamada a la API para solicitar la recuperación
            val call = RetrofitClient.apiService.forgotPassword(email)

            call.enqueue(object : Callback<LoginResponse> {

                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val recoveryRespuesta = response.body()!!

                        if (recoveryRespuesta.status == "exito") {
                            // Éxito: El servidor ha enviado el enlace/código al correo
                            Toast.makeText(this@PasswordResetActivity, recoveryRespuesta.mensaje, Toast.LENGTH_LONG).show()

                            // Opcional: Volver a la pantalla de login
                            val intent = Intent(this@PasswordResetActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Error reportado por el servidor (ej: correo no existe o fallo de lógica)
                            Toast.makeText(this@PasswordResetActivity, recoveryRespuesta.mensaje, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Error de servidor código 4xx/5xx el puto amo de los errores, te odio error 500
                        Toast.makeText(this@PasswordResetActivity, "Error en la solicitud: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e("API_RESET_ERROR", "Error del servidor: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // Error de red nos fuimos sin wifi
                    Toast.makeText(this@PasswordResetActivity, "No hay conexión a internet: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("NETWORK_RESET_ERROR", "Fallo de Retrofit", t)
                }
            })
            }
        }
}