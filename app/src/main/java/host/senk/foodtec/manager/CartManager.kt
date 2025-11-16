package host.senk.foodtec.manager

import host.senk.foodtec.model.CartItem
import host.senk.foodtec.model.ComidaItem

// ¡¡Un "object"!! ¡Este es el "Singleton"!
// ¡Solo existe UNO de estos en toda la app!
object CartManager {

    // Una lista (mutable donde guardamos los platillos
    private val items = mutableListOf<CartItem>()



    /**
     * ¡Pa' meter un platillo al carrito!
     * (Lo llamamos desde 'DetailsActivity')
     */
    fun addItem(item: ComidaItem, detalles: String) {
        // ¡Checamos si el vato ya había pedido este platillo!
        val itemExistente = items.find { it.id == item.id && it.detalles_usuario == detalles }

        if (itemExistente != null) {
            // ¡Ya lo había pedido! Nomás le subimos la cantidad
            itemExistente.cantidad++
        } else {
            // ¡Es nuevo! Armamos el "renglón" (CartItem)
            val cartItem = CartItem(
                id = item.id,
                nombre = item.nombre,
                precio_unitario = item.precio,
                imagen_url = item.imagen_url,
                cantidad = 1, // ¡La primera vez!
                detalles_usuario = detalles
            )
            // ¡Y pa' dentro!
            items.add(cartItem)
        }
    }

    /**
     * ¡Pa' jalar la lista completa!
     * (Lo llamamos desde el "Modal de la compra)
     */
    fun getItems(): List<CartItem> {
        return items
    }

    /**
     * ¡Pa' sacar el total!
     */
    fun getSubtotal(): Double {
        return items.sumOf { (it.precio_unitario.toDoubleOrNull() ?: 0.0) * it.cantidad }
    }

    /**
     * ¡Pa' borrar todo cuando se confirme el pedido!
     */
    fun clearCart() {
        items.clear()
    }


    fun removeItem(item: CartItem) {
        items.remove(item)
    }



    // ¡Aquí podríamos meter 'removeItem(item)' o 'updateCantidad(item, nuevaCantidad)'!
}