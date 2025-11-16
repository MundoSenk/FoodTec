package host.senk.foodtec.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment //  Es 'Fragment'
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import host.senk.foodtec.R
import host.senk.foodtec.adapter.PedidosAdapter //El Adapter nuevo
import host.senk.foodtec.api.RetrofitClient
import host.senk.foodtec.manager.SessionManager // El "archivero"
import host.senk.foodtec.model.Pedido
import host.senk.foodtec.model.PedidosResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PedidosFragment : Fragment() {

    // ¡Las vistas del XML!
    private lateinit var flPedidoActual: FrameLayout
    private lateinit var tvPedidoActualStatus: TextView
    private lateinit var rvPedidosAnteriores: RecyclerView

    private var usuarioId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ¡Infla (conecta) el esqueleto que armamos!
        val view = inflater.inflate(R.layout.fragment_pedidos, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  XML
        flPedidoActual = view.findViewById(R.id.flPedidoActualContainer)
        tvPedidoActualStatus = view.findViewById(R.id.tvPedidoActualStatus)
        rvPedidosAnteriores = view.findViewById(R.id.rvPedidosAnteriores)

        // JALAMOS AL VATO DEL "ARCHIVERO"
        // ¡'requireContext()' es el "Contexto" de un Fragment!
        usuarioId = SessionManager.getUserId(requireContext())

        if (usuarioId == null) {
            Toast.makeText(requireContext(), "Error: No se encontró usuario.", Toast.LENGTH_LONG).show()
            return
        }


        cargarMisPedidos()
    }

    /**
     * Llama al 'obtenerMisPedidos.php
     */
    private fun cargarMisPedidos() {
        if (usuarioId == null) return // ¡Doble chequeo!

        val call = RetrofitClient.apiService.obtenerMisPedidos(usuarioId!!)

        call.enqueue(object: Callback<PedidosResponse> {
            override fun onResponse(call: Call<PedidosResponse>, response: Response<PedidosResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val resp = response.body()!!
                    if (resp.status == "exito" && resp.pedidos != null) {



                        // El Pedido Actual el que NO está Entregado
                        val pedidoActual = resp.pedidos.find {
                            it.estatus.equals("Pendiente", ignoreCase = true) ||
                                    it.estatus.equals("En camino", ignoreCase = true) ||
                                    it.estatus.equals("Preparacion", ignoreCase = true)
                        }

                        // Los "Pedidos Anteriores" os que SÍ están "Entregado!
                        val pedidosViejos = resp.pedidos.filter {
                            it.estatus.equals("Entregado", ignoreCase = true)
                        }

                        // A PINTAR EL "PEDIDO ACTUAL
                        if (pedidoActual != null) {
                            flPedidoActual.visibility = View.VISIBLE
                            tvPedidoActualStatus.text = "Pedido: #${pedidoActual.id_pedido} (${pedidoActual.estatus})"
                            // Aquí iría el 'RecyclerView' chiquito de adentro
                        } else {
                            flPedidoActual.visibility = View.GONE ///Lo ocultamos
                        }

                        // A PINTAR LOS "PEDIDOS ANTERIORES
                        val listenerClicPedido = { pedido: Pedido ->
                            Toast.makeText(requireContext(), "Le picaste al Pedido #${pedido.id_pedido}", Toast.LENGTH_SHORT).show()
                            // Aquí iría el 'Intent' pa' ver el detalle del pedido viejo
                        }

                        rvPedidosAnteriores.layoutManager = LinearLayoutManager(requireContext())
                        rvPedidosAnteriores.adapter = PedidosAdapter(pedidosViejos, listenerClicPedido)

                    } else {
                        Toast.makeText(requireContext(), "Error del PHP: ${resp.mensaje}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error del server: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PedidosResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "SIN CONEXIÓN: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}