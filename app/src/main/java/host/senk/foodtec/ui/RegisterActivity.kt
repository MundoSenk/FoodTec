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
import host.senk.foodtec.model.RegistroResponse
import host.senk.foodtec.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ¡¡Este faltaba!! El del TabLayout
import com.google.android.material.tabs.TabLayout




class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }







        /////AQUI EMPIEZA NUESTRO REGISTRO CACHONDO
        val etNombre: EditText = findViewById(R.id.etNombre)
        val etUsuario: EditText = findViewById(R.id.etUsuarioRegistro)
        val etContra1: EditText = findViewById(R.id.etContrasenaRegistro)
        val etContra2: EditText = findViewById(R.id.etConfirmarContrasena)
        val etCorreo: EditText = findViewById(R.id.etCorreo)
        val btnRegistrar: Button = findViewById(R.id.btnRegistrar)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)


        // 1. Para que se vea seleccionada la pestaña "Registrarse"
        tabLayout.getTabAt(1)?.select()

        // 2. El listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Cuando el compa le pica a una pestaña
                if (tab?.position == 0) { // ¡Cero es "Inicio Sesion"!
                    // Si le picó a "Inicio Sesion" (la #0)
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java) // ¡Lo mandamos al Login!
                    startActivity(intent)
                    finish() // Matamos esta pa' que no regrese
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        }) // Aquí se cierra el listener del Tab


        btnRegistrar.setOnClickListener {

            val nombre = etNombre.text.toString()
            val usuario = etUsuario.text.toString()
            val pass1 = etContra1.text.toString()
            val pass2 = etContra2.text.toString()
            val correo = etCorreo.text.toString()

            ////SI ESTA VACIO PUESSS LE DECIMOS QUE NO SE PASE DE VENCHA Y LO LLENE
            if (nombre.isEmpty() || usuario.isEmpty() || pass1.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Detenemos la ejecución aquí
            }
    ///DEBEN COINCIDIR LAS CONTRAS PAY
            if (pass1 != pass2) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Detenemos la ejecución
            }

            // 3. ¡¡LLAMANDO AL CARTERO (RETROFIT)!!
            // Borramos el Toast de "Registrando..." y lo cambiamos por esto:

            // Usamos el "cartero" que creamos (RetrofitClient) y su "menú" (apiService)
            val call = RetrofitClient.apiService.registrarUsuario(nombre, usuario, pass1, correo)

            // .enqueue() manda la llamada en un hilo separado (para no congelar la app)
            call.enqueue(object : Callback<RegistroResponse> {

                // ESTO SE EJECUTA SI EL CARTERO SÍ LLEGÓ (LA APP HABLÓ CON EL HOSTING)
                override fun onResponse(call: Call<RegistroResponse>, response: Response<RegistroResponse>) {

                    if (response.isSuccessful && response.body() != null) {
                        // Si el servidor nos respondió bien (un código 200)
                        val registroRespuesta = response.body()!!

                        // Leemos el JSON que nos mandó el PHP
                        if (registroRespuesta.status == "exito") {
                            // ¡ÉXITO TOTAL! El PHP guardó en la BD
                            Toast.makeText(this@RegisterActivity, "¡Éxito! ${registroRespuesta.mensaje}", Toast.LENGTH_LONG).show()

                            // Aquí podrías cerrar esta pantalla y volver al Login
                            // finish()

                        } else {
                            // Si el PHP nos mandó un 'status: "error"'
                            Toast.makeText(this@RegisterActivity, "Error: ${registroRespuesta.mensaje}", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        // Si el servidor nos dio un error (ej. 404 - No encontró el .php, 500 - Error en PHP)
                        Toast.makeText(this@RegisterActivity, "Error en el servidor: ${response.message()}", Toast.LENGTH_LONG).show()
                        Log.e("API_ERROR", "Respuesta no exitosa: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                // ESTO SE EJECUTA SI EL CARTERO NUNCA LLEGÓ (NO HAY INTERNET, DOMINIO MAL ESCRITO)
                override fun onFailure(call: Call<RegistroResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("NETWORK_ERROR", "Fallo en la llamada Retrofit", t)
                }
            })
        }
    }
}