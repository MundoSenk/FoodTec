package host.senk.foodtec.ui

import android.os.Handler
import android.os.Looper

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    private lateinit var bottomNavView: BottomNavigationView

    // --- Lógica ---
    private lateinit var menuAdapter: MenuAdapter
    private val listaDeResultados = mutableListOf<ComidaItem>()
    private var usuarioLogueado: String = "invitado" // Pa'l Details

    private val debounceHandler: Handler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null
    private val DEBOUNCE_DELAY_MS: Long = 350

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

        setupBottomNav()

        // Configuramos los Listeners (¡El del "tache" y el del "teclado"!)
        setupListeners()
    }

    private fun bindViews() {
        btnRegresar = findViewById(R.id.btnRegresarSearch)
        etBuscador = findViewById(R.id.etBuscadorReal)
        rvResultados = findViewById(R.id.rvResultadosBusqueda)
        tvNoResultados = findViewById(R.id.tvNoResultados)
        bottomNavView = findViewById(R.id.bottomNavViewSearch)
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
        rvResultados.layoutManager = GridLayoutManager(this, 2) // ¡Vertical!
        rvResultados.adapter = menuAdapter
    }



    private fun setupBottomNav() {
        // ¡Le decimos que "Buscar" está seleccionado!
        bottomNavView.selectedItemId = R.id.nav_search

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_search -> true

                R.id.nav_pedidos -> {
                    val intent = Intent(this, PedidosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    Toast.makeText(this, "¡Perfil próximamente!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        // Botón de Regresar Tache
        btnRegresar.setOnClickListener {
            finish() // Cierra esta pantalla
        }

        //  Definimos la tarea
        // Esto es lo que se ejecutará DESPUÉS de que el vato deje de teclear
        debounceRunnable = Runnable {
            // Sacamos el texto MÁS RECIENTE del EditText
            val termino = etBuscador.text.toString().trim()

            if (termino.length > 1) {
                ejecutarBusqueda(termino)
            }
        }

        // EL LISTENER DEL TECLADO (con Debouncer)
        etBuscador.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            // Este se activa CADA VEZ que el vato teclea
            override fun afterTextChanged(s: Editable?) {

                // EL "ANTI-REBOTE"

                // Cancelamos cualquier búsqueda anterior que estuviera en cola
                // (Mata el cronómetro viejo)
                debounceRunnable?.let { debounceHandler.removeCallbacks(it) }

                // (ógica para limpiar la lista si borra todo
                val termino = s.toString().trim()
                if (termino.isEmpty()) {
                    listaDeResultados.clear()
                    menuAdapter.notifyDataSetChanged()
                    tvNoResultados.visibility = View.GONE
                }
                // Ponemos la NUEVA búsqueda en la cola con un retraso
                // (Inicia un cronómetro nuevo)
                // Usamos el 'runnable' que definimos arriba
                else if (termino.length > 1) {
                    debounceRunnable?.let { debounceHandler.postDelayed(it, DEBOUNCE_DELAY_MS) }
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