package host.senk.foodtec.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import host.senk.foodtec.R

// Hereda (extends) de "BottomSheetDialogFragment"
class CartModalFragment : BottomSheetDialogFragment() {

    // ¡Este es el onCreate de los Fragments!
    // Aquí "inflamos" (conectamos) el XML que vamos a armar
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ¡Le decimos qué XML va a usar
        val view = inflater.inflate(R.layout.fragment_cart_modal, container, false)
        return view
    }

    // ¡Este es el onCreateView de los Fragments!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}