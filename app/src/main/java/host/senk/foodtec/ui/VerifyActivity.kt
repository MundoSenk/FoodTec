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
import host.senk.foodtec.R
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.model.RegistroResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

////HERMANO EL SESSION
import host.senk.foodtec.manager.SessionManager

// ¡¡Este faltaba!! El del TabLayout
import com.google.android.material.tabs.TabLayout


class VerifyActivity : AppCompatActivity() {

    private var correoDelUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val etCodigo: EditText = findViewById(R.id.etCodigo)
        val btnVerificar: Button = findViewById(R.id.btnVerificar)
        val tvAyuda: TextView = findViewById(R.id.tvAyudaVerificar)

        // correo que venia en la mochila
        correoDelUsuario = intent.getStringExtra("CORREO_USUARIO")

        // 2validamos que haya llegado
        if (correoDelUsuario == null) {
            Toast.makeText(this, "Error: No se recibió el correo", Toast.LENGTH_LONG).show()
            finish() // ¡Pa'tras!
            return
        }


        tvAyuda.text = "¡Revisa tu correo ($correoDelUsuario)! Te mandamos un código."


        btnVerificar.setOnClickListener {
            val codigo = etCodigo.text.toString().trim()

            // POR SI METIO MENOS JAJA
            if (codigo.length < 6) {
                Toast.makeText(this, "El código debe ser de 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // API EN CAMINO
            val call = RetrofitClient.apiService.verificarCodigo(correoDelUsuario!!, codigo)

            call.enqueue(object: Callback<RegistroResponse> {
                override fun onResponse(call: Call<RegistroResponse>, response: Response<RegistroResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val resp = response.body()!!

                        if (resp.status == "exito") {

                            // ¡Checamos que el PHP SÍ nos mandó los datos!
                            if (resp.usuario != null && resp.nombre != null) {

                                // ¡A "GUARDAR" AL VATO!
                                SessionManager.saveUser(
                                    this@VerifyActivity,
                                    resp.usuario,
                                    resp.nombre
                                )

                                Toast.makeText(this@VerifyActivity, "¡Cuenta activada! ¡Bienvenido, ${resp.nombre}!", Toast.LENGTH_LONG).show()

                                // ¡Ahora sí, al Home!
                                val intent = Intent(this@VerifyActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finishAffinity() // KILL A LOGIN Y REGISTRO

                            } else {
                                // ¡El PHP dijo "exito" pero no mandó los datos! ¡Qué pendejo!
                                Toast.makeText(this@VerifyActivity, "¡Verificado! Pero hubo un error al jalar tus datos.", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@VerifyActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finishAffinity()
                            }

                        } else {
                            // SI ESTABA MAL (Código incorrecto, etc.)
                            Toast.makeText(this@VerifyActivity, "Error: ${resp.mensaje}", Toast.LENGTH_LONG).show()
                        }

                    } else { // <-- ¡¡ESTA LLAVE CIERRA el 'if (response.isSuccessful...)'!!

                        // COMO PUEDE PASAR SIEMPRE Y MUERE EL SERVER
                        Toast.makeText(this@VerifyActivity, "Error del server: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e("API_ERROR_VERIFY", "El server se murió: ${response.errorBody()?.string()}")
                    }
                } // ¡¡AQUÍ CIERRA EL 'onResponse'!!

                override fun onFailure(call: Call<RegistroResponse>, t: Throwable) {
                    // S NO HAY CONEXION JJA
                    Toast.makeText(this@VerifyActivity, "SIN CONEXIÓN AL SERVIDOR: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("NETWORK_ERROR_VERIFY", "Falló Retrofit", t)
                }
            })
        }
    } // ¡¡AQUÍ CIERRA EL 'onCreate'!!
} // ¡¡AQUÍ CIERRA LA 'VerifyActivity'!!