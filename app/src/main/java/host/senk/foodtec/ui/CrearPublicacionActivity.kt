package host.senk.foodtec.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yalantis.ucrop.UCrop // <-- IMPORTANTE
import host.senk.foodtec.R
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.CrearPedidoResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CrearPublicacionActivity : AppCompatActivity() {

    private lateinit var ivFoto: ImageView
    private lateinit var etTitulo: EditText
    private lateinit var etDesc: EditText
    private lateinit var rgTipo: RadioGroup
    private lateinit var btnPublicar: Button
    private lateinit var tvInstruccion: TextView

    // Uri final (ya recortada) lista para subir
    private var uriFinalRecortada: Uri? = null

    // 1. LANZADOR DE GALERÍA
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // En lugar de mostrarla directo, la mandamos al "Quirófano" (Recorte)
            iniciarRecorte(uri)
        }
    }

    // 2. LANZADOR DEL RECORTE (uCrop)
    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                // ¡ÉXITO! Tenemos la foto recortada
                uriFinalRecortada = resultUri
                ivFoto.setImageURI(resultUri)
                tvInstruccion.visibility = android.view.View.GONE
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(this, "Error al recortar: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_publicacion)

        ivFoto = findViewById(R.id.ivFotoPreview)
        tvInstruccion = findViewById(R.id.tvInstruccionFoto)
        etTitulo = findViewById(R.id.etTituloPub)
        etDesc = findViewById(R.id.etDescPub)
        rgTipo = findViewById(R.id.rgTipoPub)
        btnPublicar = findViewById(R.id.btnPublicar)

        ivFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnPublicar.setOnClickListener {
            validarYSubir()
        }
    }

    /**
     * Configura uCrop con colores FoodTec y lanza la actividad
     */
    private fun iniciarRecorte(sourceUri: Uri) {
        val destinoFileName = "recorte_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, destinoFileName))

        val options = UCrop.Options()

        options.setToolbarColor(ContextCompat.getColor(this, R.color.foodtec_azul))
        options.setStatusBarColor(ContextCompat.getColor(this, android.R.color.black))
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.foodtec_azul))

        options.setToolbarTitle("Ajustar Imagen")
        options.setFreeStyleCropEnabled(true)

        val intent = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .getIntent(this)

        cropImageLauncher.launch(intent)
    }

    private fun validarYSubir() {
        val titulo = etTitulo.text.toString().trim()
        val desc = etDesc.text.toString().trim()
        val tipo = if (rgTipo.checkedRadioButtonId == R.id.rbPerdido) "Perdido" else "Encontrado"

        if (titulo.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
            return
        }
        if (uriFinalRecortada == null) {
            Toast.makeText(this, "¡La foto es obligatoria!", Toast.LENGTH_SHORT).show()
            return
        }

        subirAlServidor(titulo, desc, tipo)
    }

    private fun subirAlServidor(titulo: String, desc: String, tipo: String) {
        val userId = SessionManager.getUserId(this) ?: return
        val telefono = SessionManager.getPhone(this) ?: return

        btnPublicar.isEnabled = false
        btnPublicar.text = "SUBIENDO..."

        val rbUser = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbTitulo = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbDesc = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbContacto = telefono.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbTipo = tipo.toRequestBody("text/plain".toMediaTypeOrNull())

        // Aquí ya usamos la URI RECORTADA que apunta a un archivo real
        // No necesitamos la función compleja de antes porque uCrop ya crea un File real
        val file = File(uriFinalRecortada!!.path!!)

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val bodyImagen = MultipartBody.Part.createFormData("imagen", file.name, requestFile)

        RetrofitClient.apiService.crearPublicacion(rbUser, rbTitulo, rbDesc, rbContacto, rbTipo, bodyImagen)
            .enqueue(object : Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    if (response.isSuccessful && response.body()?.status == "exito") {

                        ///eliminamos el cache de la foto que cortamos con el uCrop
                        try { file.delete() } catch (e: Exception) { e.printStackTrace() }
                        Toast.makeText(this@CrearPublicacionActivity, "¡Publicado!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        val msg = response.body()?.mensaje ?: "Error"
                        Toast.makeText(this@CrearPublicacionActivity, msg, Toast.LENGTH_LONG).show()
                        btnPublicar.isEnabled = true
                        btnPublicar.text = "PUBLICAR AHORA"
                    }
                }

                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    // ¡Imprime el error completo en el Logcat!
                    Log.e("API_ERROR", "Tronó la subida", t)
                    Toast.makeText(this@CrearPublicacionActivity, "Fallo: ${t.message}", Toast.LENGTH_LONG).show()
                    btnPublicar.isEnabled = true
                    btnPublicar.text = "PUBLICAR AHORA"
                }
            })
    }
}