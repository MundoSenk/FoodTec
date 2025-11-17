package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import host.senk.foodtec.R
import host.senk.foodtec.api.ApiService
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.CrearPedidoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupFoodterActivity : AppCompatActivity() {

    // Vistas del XML
    private lateinit var btnRegresar: ImageButton
    private lateinit var rbAmabilidad: RatingBar
    private lateinit var rbInteractuar: RatingBar
    private lateinit var switchPropinas: SwitchMaterial
    private lateinit var etPorqueFoodter: EditText
    private lateinit var btnHabilitarme: Button

    // API y Sesión
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }
    private lateinit var usuarioId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_foodter)

        // Sacamos al vato del "archivero"
        val id = SessionManager.getUserId(this)
        if (id == null) {
            // Si no hay ID, no debería estar aquí. ¡Pa' fuera!
            Toast.makeText(this, "Error: No se encontró usuario", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        usuarioId = id // Guardamos el ID

        // Amarramos" las vistas
        bindViews()

        // Alambramos" los botones
        setupListeners()
    }

    private fun bindViews() {
        btnRegresar = findViewById(R.id.btnRegresarSignup)
        rbAmabilidad = findViewById(R.id.rbAmabilidad)
        rbInteractuar = findViewById(R.id.rbInteractuar)
        switchPropinas = findViewById(R.id.switchPropinas)
        etPorqueFoodter = findViewById(R.id.etPorqueFoodter)
        btnHabilitarme = findViewById(R.id.btnHabilitarme)
    }

    private fun setupListeners() {
        // Botón de tache
        btnRegresar.setOnClickListener {
            finish() // Cierra esta pantalla
        }


        btnHabilitarme.setOnClickListener {
            validarYEnviarFormulario()
        }
    }

    /**
     * ¡El "cadenaro" del formulario!
     */
    private fun validarYEnviarFormulario() {
        // Jalamos los datos del formulario
        val amabilidad = rbAmabilidad.rating.toInt()
        val interactuar = rbInteractuar.rating.toInt()
        val aceptaPropinasBool = switchPropinas.isChecked
        val porqueTexto = etPorqueFoodter.text.toString().trim()

        // Validaciones (¡El vato tiene que llenar todo!)
        if (amabilidad == 0) {
            Toast.makeText(this, "¡Oye! Dinos qué tan amable eres", Toast.LENGTH_SHORT).show()
            return
        }
        if (interactuar == 0) {
            Toast.makeText(this, "¡Oye! Dinos qué tan fácil interactúas", Toast.LENGTH_SHORT).show()
            return
        }
        if (!aceptaPropinasBool) {
            Toast.makeText(this, "Debes aceptar la política de propinas", Toast.LENGTH_SHORT).show()
            return
        }
        if (porqueTexto.isEmpty()) {
            Toast.makeText(this, "¡Oye! Cuéntanos por qué quieres ser Foodter", Toast.LENGTH_SHORT).show()
            etPorqueFoodter.error = "Campo requerido"
            return
        }

        // Convertimos el booleano a INT (1 o 0) pal PHP
        val aceptaPropinasInt = if (aceptaPropinasBool) 1 else 0

        // Si todo está bien... ¡A la API!
        enviarFormularioAPI(amabilidad, interactuar, aceptaPropinasInt, porqueTexto)
    }

    /**
     * ¡Llama al habilitarFoodter.php!
     */
    private fun enviarFormularioAPI(amabilidad: Int, interactuar: Int, aceptaPropinasInt: Int, porqueTexto: String) {

        btnHabilitarme.isEnabled = false // Desactivamos el botón pa' que no le piquen 2 veces
        btnHabilitarme.text = "REGISTRANDO..."

        val call = apiService.habilitarFoodter(usuarioId, amabilidad, interactuar, aceptaPropinasInt, porqueTexto)

        call.enqueue(object : Callback<CrearPedidoResponse> {
            override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                if (response.isSuccessful && response.body()?.status == "exito") {
                    // ÉXITO
                    Toast.makeText(this@SignupFoodterActivity, "¡Felicidades! Ahora eres Foodter", Toast.LENGTH_LONG).show()

                    // Actualizamos el "archivero" AL INSTANTE
                    SessionManager.setFoodterStatus(this@SignupFoodterActivity, true)

                    // Lo mandamos a la (futura) pantalla de chamba

                    val intent = Intent(this@SignupFoodterActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    finish()

                } else {
                    // El PHP tronó (ej. 'Duplicate entry' si ya estaba registrado)
                    val errorMsg = response.body()?.mensaje ?: "Error del servidor: ${response.code()}"
                    Toast.makeText(this@SignupFoodterActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    btnHabilitarme.isEnabled = true
                    btnHabilitarme.text = "¡QUIERO SER FOODTER!"
                }
            }

            override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                // No hay net
                Log.e("SignupFoodter", "Fallo de red", t)
                Toast.makeText(this@SignupFoodterActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                btnHabilitarme.isEnabled = true
                btnHabilitarme.text = "¡QUIERO SER FOODTER!"
            }
        })
    }
}