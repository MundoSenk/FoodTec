package host.senk.foodtec.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.adapter.MenuAdapter // ¡¡REUTILIZAMOS EL ADAPTER!!
import host.senk.foodtec.api.ApiService
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager // ¡Lo ocupamos pa'l Details!
import host.senk.foodtec.model.ComidaItem
import host.senk.foodtec.model.MenuResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    // --- Vistas ---
    private lateinit var btnRegresar: ImageButton
    private lateinit var etBuscador: EditText
    private lateinit var rvResultados: RecyclerView
    private lateinit var tvNoResultados: TextView

    // --- Lógica ---
    private lateinit var menuAdapter: MenuAdapter
    private val listaDeResultados = mutableListOf<ComidaItem>()
    private var usuarioLogueado: String = "invitado" // Pa'l Details

    // API
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // OJO: Esta NO usa enableEdgeToEdge para evitar problemas con el teclado
        setContentView(R.layout.activity_search)

        // Jalamos el ID del vato (pa' mandarlo al DetailsActivity)
        usuarioLogueado = SessionManager.getUserId(this) ?: "invitado_error"

        // Amarramos Vistas
        bindViews()

        // Configuramos el RecyclerView (¡REUTILIZANDO!)
        setupRecyclerView()

        // Configuramos los Listeners (¡El del "tache" y el del "teclado"!)
        setupListeners()
    }

    private fun bindViews() {
        btnRegresar = findViewById(R.id.btnRegresarSearch)
        etBuscador = findViewById(R.id.etBuscadorReal)
        rvResultados = findViewById(R.id.rvResultadosBusqueda)
        tvNoResultados = findViewById(R.id.tvNoResultados)
    }

    private fun setupRecyclerView() {

        // Usamos el MISMO MenuAdapter que el HomeActivity

        // Definimos el listener
        val listenerDelClick = { comidaItem: ComidaItem ->
            val intent = Intent(this@SearchActivity, DetailsActivity::class.java)
            intent.putExtra("COMIDA_SELECCIONADA", comidaItem)
            intent.putExtra("USER_NAME", usuarioLogueado) // ¡Le pasamos el vato!
            startActivity(intent)
        }

        // Creamos el adapter
        menuAdapter = MenuAdapter(listaDeResultados, listenerDelClick)

        // Lo conectamos al RecyclerView
        rvResultados.layoutManager = LinearLayoutManager(this) // ¡Vertical!
        rvResultados.adapter = menuAdapter
    }

    private fun setupListeners() {
        // Botón de Regresar
        btnRegresar.setOnClickListener {
            finish()
        }

        // EL LISTENER DEL TECLADO
        etBuscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            //Este es el bueno! Se activa DESPUÉS de que el vato escribió
            override fun afterTextChanged(s: Editable?) {
                val termino = s.toString().trim()

                // ¡Para no spamear al server si está vacío o muy corto!
                if (termino.length > 1) {
                    ejecutarBusqueda(termino)
                } else if (termino.isEmpty()) {

                    listaDeResultados.clear()
                    menuAdapter.notifyDataSetChanged()
                    tvNoResultados.visibility = View.GONE
                }
            }
        })
    }

    /**
     * ¡La función que llama al PHP!
     */
    private fun ejecutarBusqueda(termino: String) {
        Log.d("SearchActivity", "Buscando término: $termino")

        apiService.buscarMenu(termino).enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()!!
                    if (resp.status == "exito" && resp.menu != null) {

                       //ÉXITO! Limpiamos la lista vieja
                        listaDeResultados.clear()

                        if (resp.menu.isEmpty()) {
                            // No se encontró nada
                            Log.d("SearchActivity", "El PHP no regresó resultados.")
                            tvNoResultados.visibility = View.VISIBLE
                        } else {
                            // Encontramos Llenamos la lista nueva
                            Log.d("SearchActivity", "Se encontraron ${resp.menu.size} resultados")
                            tvNoResultados.visibility = View.GONE
                            listaDeResultados.addAll(resp.menu)
                        }

                        // Avisamos al adapter que se repinte
                        menuAdapter.notifyDataSetChanged()

                    } else {
                        Toast.makeText(this@SearchActivity, resp.mensaje ?: "Error del PHP", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SearchActivity, "Error del server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                Toast.makeText(this@SearchActivity, "Error de Red: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("SearchActivity", "Fallo de Retrofit", t)
            }
        })
    }
}