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
import host.senk.foodtec.model.Publicacion
import host.senk.foodtec.model.PublicacionesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ObjetosPerdidosActivity : AppCompatActivity() {

    private lateinit var rvPublicaciones: RecyclerView
    private lateinit var fabAgregar: FloatingActionButton
    private val listaPublicaciones = mutableListOf<Publicacion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objetos_perdidos)

        rvPublicaciones = findViewById(R.id.rvPublicaciones)
        fabAgregar = findViewById(R.id.fabAgregarPublicacion)


        val btnRegresar: android.view.View = findViewById(R.id.btnRegresarObjetos)
        btnRegresar.setOnClickListener {
            finish()
        }

        rvPublicaciones.layoutManager = LinearLayoutManager(this)
        val adapter = PublicacionesAdapter(listaPublicaciones)
        rvPublicaciones.adapter = adapter

        fabAgregar.setOnClickListener {
            val intent = Intent(this, CrearPublicacionActivity::class.java)
            startActivity(intent)
        }

        cargarDatos(adapter)
    }


    override fun onResume() {
        super.onResume()

        // cargarDatos(adapter)

    }

    private fun cargarDatos(adapter: PublicacionesAdapter) {
        RetrofitClient.apiService.obtenerPublicaciones().enqueue(object : Callback<PublicacionesResponse> {
            override fun onResponse(call: Call<PublicacionesResponse>, response: Response<PublicacionesResponse>) {
                if (response.isSuccessful && response.body()?.status == "exito") {
                    listaPublicaciones.clear()
                    response.body()?.publicaciones?.let { listaPublicaciones.addAll(it) }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ObjetosPerdidosActivity, "No hay publicaciones aún", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PublicacionesResponse>, t: Throwable) {
                Toast.makeText(this@ObjetosPerdidosActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}