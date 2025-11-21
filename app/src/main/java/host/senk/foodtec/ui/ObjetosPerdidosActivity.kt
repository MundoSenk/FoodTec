package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import host.senk.foodtec.R
import host.senk.foodtec.adapter.PublicacionesAdapter
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.CrearPedidoResponse
import host.senk.foodtec.model.Publicacion
import host.senk.foodtec.model.PublicacionesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ObjetosPerdidosActivity : AppCompatActivity() {

    private lateinit var rvPublicaciones: RecyclerView
    private lateinit var fabAgregar: FloatingActionButton
    private val listaPublicaciones = mutableListOf<Publicacion>()

    // Hacemos el adapter propiedad de la clase para acceder a él desde cualquier lado
    private lateinit var adapter: PublicacionesAdapter
    private var miId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objetos_perdidos)

        miId = SessionManager.getUserId(this) ?: ""

        rvPublicaciones = findViewById(R.id.rvPublicaciones)
        fabAgregar = findViewById(R.id.fabAgregarPublicacion)
        val btnRegresar = findViewById<android.view.View>(R.id.btnRegresarObjetos)

        rvPublicaciones.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adapter
        adapter = PublicacionesAdapter(listaPublicaciones, miId) { publicacion ->
            confirmarBorrado(publicacion)
        }
        rvPublicaciones.adapter = adapter

        fabAgregar.setOnClickListener {
            startActivity(Intent(this, CrearPublicacionActivity::class.java))
        }
        btnRegresar.setOnClickListener { finish() }
    }

    // --- REFRESH AUTOMÁTICO ---
    override fun onResume() {
        super.onResume()
        // Llamamos a cargarDatos cada vez que la pantalla se vuelve visible
        cargarDatos()
    }

    // Esta función NO debe recibir argumentos, usa el adapter global
    private fun cargarDatos() {
        RetrofitClient.apiService.obtenerPublicaciones().enqueue(object : Callback<PublicacionesResponse> {
            override fun onResponse(call: Call<PublicacionesResponse>, response: Response<PublicacionesResponse>) {
                if (response.isSuccessful && response.body()?.status == "exito") {
                    listaPublicaciones.clear()
                    response.body()?.publicaciones?.let { listaPublicaciones.addAll(it) }
                    adapter.notifyDataSetChanged()
                } else {
                    // Si no hay publicaciones, no mostramos error, solo lista vacía
                    // Toast.makeText(this@ObjetosPerdidosActivity, "No hay publicaciones", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PublicacionesResponse>, t: Throwable) {
                Toast.makeText(this@ObjetosPerdidosActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- LÓGICA DE BORRADO (AFUERA DE RETROFIT) ---
    private fun confirmarBorrado(pub: Publicacion) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("¿Borrar publicación?")
            .setMessage("Esta acción no se puede deshacer.")
            .setPositiveButton("Borrar") { _, _ ->
                ejecutarBorradoAPI(pub)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarBorradoAPI(pub: Publicacion) {
        RetrofitClient.apiService.borrarPublicacion(pub.id_publicacion, miId)
            .enqueue(object : Callback<CrearPedidoResponse> {
                override fun onResponse(call: Call<CrearPedidoResponse>, response: Response<CrearPedidoResponse>) {
                    if (response.isSuccessful && response.body()?.status == "exito") {
                        Toast.makeText(this@ObjetosPerdidosActivity, "Eliminado", Toast.LENGTH_SHORT).show()
                        cargarDatos() // Recargamos la lista
                    } else {
                        Toast.makeText(this@ObjetosPerdidosActivity, "Error al borrar", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CrearPedidoResponse>, t: Throwable) {
                    Toast.makeText(this@ObjetosPerdidosActivity, "Fallo de red", Toast.LENGTH_SHORT).show()
                }
            })
    }
}