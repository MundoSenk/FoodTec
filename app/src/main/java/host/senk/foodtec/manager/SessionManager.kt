package host.senk.foodtec.manager

import android.content.Context
import android.content.SharedPreferences

// ¡Archivero v3.0 (Con Valoraciones)!
object SessionManager {

    private const val PREFS_NAME = "FoodtecPrefs"

    // Las "llaves"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_IS_FOODTER = "is_foodter"
    private const val KEY_VAL_CLIENTE = "valoracion_cliente"
    private const val KEY_VAL_FOODTER = "valoracion_foodter"
    private const val KEY_AVATAR_ID = "avatar_id"

    private const val KEY_HAS_ACTIVE_ORDER = "has_active_order"
    private const val KEY_PHONE = "user_phone"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * ¡Función "gorda" v3.0!
     */
    fun saveUser(context: Context, usuarioId: String, nombre: String, esFoodter: Boolean, valCliente: Float, valFoodter: Float,
                 avatarId: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_USER_ID, usuarioId)
        editor.putString(KEY_USER_NAME, nombre)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putBoolean(KEY_IS_FOODTER, esFoodter)
        editor.putFloat(KEY_VAL_CLIENTE, valCliente)
        editor.putFloat(KEY_VAL_FOODTER, valFoodter)
        editor.putString(KEY_AVATAR_ID, avatarId)
        editor.apply()
    }




    // (isLoggedIn queda igual)
    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Funciones del Foodter (quedan igual)
    fun isFoodter(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_FOODTER, false)
    }


    // Función para guardar el teléfono
    fun setPhone(context: Context, phone: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_PHONE, phone)
        editor.apply()
    }

    fun setFoodterStatus(context: Context, status: Boolean) {
        val editor = getPrefs(context).edit()
        editor.putBoolean(KEY_IS_FOODTER, status)
        editor.apply()
    }

    //  Funciones de Jalar Datos (¡con 2 nuevas!)
    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }
    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_NAME, null)
    }

    fun getAvatarId(context: Context): String {
        return getPrefs(context).getString(KEY_AVATAR_ID, "avatar_default") ?: "avatar_default"
    }

    fun setAvatarId(context: Context, avatarId: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_AVATAR_ID, avatarId)
        editor.apply()
    }

    /**
     * ¡Pa' jalar la valoración del Cliente! (3.0 por defecto)
     */
    fun getValoracionCliente(context: Context): Float {
        return getPrefs(context).getFloat(KEY_VAL_CLIENTE, 3.0f)
    }

    /**
     * ¡Pa' jalar la valoración del Foodter! (3.0 por defecto)
     */
    fun getValoracionFoodter(context: Context): Float {
        return getPrefs(context).getFloat(KEY_VAL_FOODTER, 3.0f)
    }


    /**
    *NUEVA FUNCIÓN!!
    * La llamamos 'true' cuando se crea un pedido
    * La llamamos 'false' cuando se entrega o cancela
    */
    fun setHasActiveOrder(context: Context, hasActive: Boolean) {
        val editor = getPrefs(context).edit()
        editor.putBoolean(KEY_HAS_ACTIVE_ORDER, hasActive)
        editor.apply()
    }

    /**
     * ¡¡NUEVA FUNCIÓN!!
     * Pa' checar el "candado" antes de abrir el modal.
     */
    fun getHasActiveOrder(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_ACTIVE_ORDER, false)
    }

    // Función para leer el teléfono
    fun getPhone(context: Context): String? {
        return getPrefs(context).getString(KEY_PHONE, null)
    }


    // (logout queda igual)
    fun logout(context: Context) {
        val editor = getPrefs(context).edit()
        editor.clear()
        editor.apply()
    }
}