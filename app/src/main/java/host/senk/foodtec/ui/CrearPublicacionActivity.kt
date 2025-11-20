package host.senk.foodtec.ui

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import java.io.FileOutputStream

class CrearPublicacionActivity : AppCompatActivity() {

    private lateinit var ivFoto: ImageView
    private lateinit var etTitulo: EditText
    private lateinit var etDesc: EditText
    private lateinit var rgTipo: RadioGroup
    private lateinit var btnPublicar: Button

    private var imageUri: Uri? = null // Aquí guardamos la uri temporal

    // El "lanzador" de la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            ivFoto.setImageURI(uri) // Mostramos la previsualización
            findViewById<TextView>(R.id.tvInstruccionFoto).visibility = android.view.View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_publicacion)

        ivFoto = findViewById(R.id.ivFotoPreview)
        etTitulo = findViewById(R.id.etTituloPub)
        etDesc = findViewById(R.id.etDescPub)
        rgTipo = findViewById(R.id.rgTipoPub)
        btnPublicar = findViewById(R.id.btnPublicar)

        // Al picarle a la imagen -> Abrir Galería
        ivFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Al picarle a Publicar
        btnPublicar.setOnClickListener {
            validarYSubir()
        }
    }

    private fun validarYSubir() {
        val titulo = etTitulo.text.toString().trim()
        val desc = etDesc.text.toString().trim()
        val tipo = if (rgTipo.checkedRadioButtonId == R.id.rbPerdido) "Perdido" else "Encontrado"

        if (titulo.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Llena el título y la descripción", Toast.LENGTH_SHORT).show()
            return
        }
        if (imageUri == null) {
            Toast.makeText(this, "¡La foto es obligatoria!", Toast.LENGTH_SHORT).show()
            return
        }

        subirAlServidor(titulo, desc, tipo)
    }

    private fun subirAlServidor(titulo: String, desc: String, tipo: String) {
        // Datos de sesión
        val userId = SessionManager.getUserId(this) ?: return
        val telefono = SessionManager.getPhone(this) ?: return

        btnPublicar.isEnabled = false
        btnPublicar.text = "SUBIENDO..."

        // Convertimos Textos a RequestBody
        val rbUser = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbTitulo = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbDesc = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbContacto = telefono.toRequestBody("text/plain".toMediaTypeOrNull())
        val rbTipo = tipo.toRequestBody("text/plain".toMediaTypeOrNull())

        // Convertimos Imagen a Multipart (LA PARTE DIFÍCIL)
        val file = getFileFromUri(imageUri!!)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val bodyImagen = MultipartBody.Part.createFormData("imagen", file.name, requestFile)

        // Llamada Retrofit
        RetrofitClient.apiService.crearPublicacion(rbUser, rbTitulo, rbDesc, rbContacto, rbTipo, bodyImagen)
            .enqueue(object : Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    if (response.isSuccessful && response.body()?.status == "exito") {
                        Toast.makeText(this@CrearPublicacionActivity, "¡Publicado!", Toast.LENGTH_LONG).show()
                        finish() // Regresamos a la lista
                    } else {
                        val msg = response.body()?.mensaje ?: "Error del servidor"
                        Toast.makeText(this@CrearPublicacionActivity, "Error: $msg", Toast.LENGTH_LONG).show()
                        btnPublicar.isEnabled = true
                        btnPublicar.text = "PUBLICAR AHORA"
                    }
                }

                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    Toast.makeText(this@CrearPublicacionActivity, "Fallo de red: ${t.message}", Toast.LENGTH_LONG).show()
                    btnPublicar.isEnabled = true
                    btnPublicar.text = "PUBLICAR AHORA"
                }
            })
    }

    // FUNCIÓN MÁGICA: Convierte URI (content://) a File real
    private fun getFileFromUri(uri: Uri): File {
        val contentResolver = applicationContext.contentResolver
        val fileName = getFileName(uri)

        // Creamos un archivo temporal en la caché de la app
        val tempFile = File(applicationContext.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tempFile
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_image.jpg"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}