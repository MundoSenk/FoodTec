package host.senk.foodtec.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.adapter.NotificacionesAdapter
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager
import host.senk.foodtec.model.Notificacion
import host.senk.foodtec.model.NotificacionesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var rvNotificaciones: RecyclerView
    private lateinit var tvVacio: TextView
    private val listaNotif = mutableListOf<Notificacion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        val btnRegresar = findViewById<ImageButton>(R.id.btnRegresarNotif)
        rvNotificaciones = findViewById(R.id.rvNotificaciones)
        tvVacio = findViewById(R.id.tvNoNotif)

        btnRegresar.setOnClickListener { finish() }

        setupRecyclerView()
        cargarNotificaciones()
    }

    private fun setupRecyclerView() {
        rvNotificaciones.layoutManager = LinearLayoutManager(this)
        val adapter = NotificacionesAdapter(listaNotif)
        rvNotificaciones.adapter = adapter
    }

    private fun cargarNotificaciones() {
        val userId = SessionManager.getUserId(this) ?: return

        RetrofitClient.apiService.obtenerMisNotificaciones(userId)
            .enqueue(object : Callback<NotificacionesResponse> {
                override fun onResponse(call: Call<NotificacionesResponse>, response: Response<NotificacionesResponse>) {
                    if (response.isSuccessful && response.body()?.status == "exito") {
                        val notis = response.body()?.notificaciones

                        if (!notis.isNullOrEmpty()) {
                            listaNotif.clear()
                            listaNotif.addAll(notis)
                            rvNotificaciones.adapter?.notifyDataSetChanged()
                            tvVacio.visibility = View.GONE
                        } else {
                            tvVacio.visibility = View.VISIBLE
                        }
                    } else {
                        tvVacio.text = "Error al cargar."
                        tvVacio.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<NotificacionesResponse>, t: Throwable) {
                    Toast.makeText(this@NotificacionesActivity, "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
    }
}