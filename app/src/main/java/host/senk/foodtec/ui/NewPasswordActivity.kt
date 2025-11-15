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

class NewPasswordActivity : AppCompatActivity() {

    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. OBTENER EL TOKEN DEL DEEP LINK
        val data = intent?.data
        val token = data?.getQueryParameter("token")

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token inválido o ausente", Toast.LENGTH_SHORT).show()
            return
        }



        // 2. REFERENCIAS UI
        val etNuevaPassword: EditText = findViewById(R.id.etNuevaPassword)
        val etConfirmarPassword: EditText = findViewById(R.id.etConfirmarPassword)
        val btnGuardar: Button = findViewById(R.id.btnGuardarPassword)


        // 3. BOTÓN PRESIONADO

        btnGuardar.setOnClickListener {

            val pass1 = etNuevaPassword.text.toString().trim()
            val pass2 = etConfirmarPassword.text.toString().trim()

            if (pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass1 != pass2) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // 4. LLAMAR API PARA CAMBIAR CONTRASEÑA

            RetrofitClient.apiService.resetPassword(token, pass1)
                .enqueue(object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val r = response.body()!!

                            Toast.makeText(this@NewPasswordActivity, r.mensaje, Toast.LENGTH_LONG).show()

                            if (r.status == "exito") {
                                val intent = Intent(this@NewPasswordActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } else {
                            Toast.makeText(
                                this@NewPasswordActivity,
                                "Error en servidor: ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("RESET_ERROR", response.errorBody()?.string() ?: "null")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(
                            this@NewPasswordActivity,
                            "Error de conexión: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("RESET_FAIL", "Error Retrofit", t)
                    }
                })
            }
        }
}