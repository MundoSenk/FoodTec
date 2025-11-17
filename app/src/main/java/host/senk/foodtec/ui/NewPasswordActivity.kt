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

// --- ¡¡AQUÍ ESTÁ EL "ARCHIVERO", PA!! ---
import host.senk.foodtec.manager.SessionManager

class NewPasswordActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  OBTENER EL TOKEN DEL DEEP LINK
        val data = intent?.data
        val token = data?.getQueryParameter("token") // ¡Esta es la buena!

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token inválido", Toast.LENGTH_SHORT).show()
            finish() // ¡Lo matamos si no hay token!
            return
        }

        //  REFERENCIAS UI
        val etNuevaPassword: EditText = findViewById(R.id.etNuevaPassword)
        val etConfirmarPassword: EditText = findViewById(R.id.etConfirmarPassword)
        val btnGuardar: Button = findViewById(R.id.btnGuardarPassword)


        // BOTÓN PRESIONADO
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

            // LLAMAR API PARA CAMBIAR CONTRASEÑA
            RetrofitClient.apiService.resetPassword(token, pass1)
                .enqueue(object : Callback<LoginResponse> {

                    // --- ¡¡AQUÍ ESTÁ EL ARREGLO, PA!! ---
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val r = response.body()!!
                            Toast.makeText(this@NewPasswordActivity, r.mensaje, Toast.LENGTH_LONG).show()

                            if (r.status == "exito") {

                                // ¡Checamos que el PHP SÍ nos mandó los datos!
                                if (r.usuario != null && r.nombre != null) {

                                    // --- ¡¡EL CAMBIO ESTÁ AQUÍ, HERMANO!! ---

                                    // ¡Asumimos 'false' porque el PHP de reset no nos manda ese dato!
                                    val esFoodter = r.es_foodter ?: false // <-- ¡ANTIBALA!

                                    SessionManager.saveUser(
                                        this@NewPasswordActivity,
                                        r.usuario,
                                        r.nombre,
                                        esFoodter // ¡¡Y LE PASAMOS EL DATO!!
                                    )


                                    val intent = Intent(this@NewPasswordActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity() // ¡KILL A TODO!

                                } else {
                                    ///fue un exito pero no mando los datos
                                    Toast.makeText(this@NewPasswordActivity, "¡Contraseña cambiada! Pero no pudimos iniciar sesión.", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@NewPasswordActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
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