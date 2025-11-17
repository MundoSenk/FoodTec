package host.senk.foodtec.manager

import android.content.Context
import android.content.SharedPreferences

// Otro Singleton el archivero (¡Ahora más mamalón!)
object SessionManager {

    // El nombre de nuestro archivero secreto
    private const val PREFS_NAME = "FoodtecPrefs"

    // Las "llaves" de los cajones
    private const val KEY_USER_ID = "user_id"       // ¡Pa' guardar "RENO"!
    private const val KEY_USER_NAME = "user_name"   // ¡Pa' guardar "Harold Mundo"!
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_IS_FOODTER = "is_foodter"

    // Un 'jale' pa'
    // Ojo Ocupa el Contexto (la Activity) pa saber dónde guardar
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }



    /**
     * El vato ya entró A guardarlo (¡Ahora con el 'es_foodter'!)
     * (Lo llamamos desde 'LoginActivity')
     */
    fun saveUser(context: Context, usuarioId: String, nombre: String, esFoodter: Boolean) { // <-- ¡NUEVO PARÁMETRO!
        val editor = getPrefs(context).edit()
        editor.putString(KEY_USER_ID, usuarioId)
        editor.putString(KEY_USER_NAME, nombre)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putBoolean(KEY_IS_FOODTER, esFoodter)
        editor.apply() // ¡Guarda!
    }

    /**
     * (Este lo usamos al abrir la app pa saltar el Login)
     */
    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // --- ¡¡NUEVAS FUNCIONES PARA EL FOODTER!! ---

    /**
     * Pa' checar si el vato es Foodter!
     * (Lo usaremos en 'HomeActivity' pa'l botón naranja)
     */
    fun isFoodter(context: Context): Boolean {
        // Devuelve 'false' si no lo encuentra
        return getPrefs(context).getBoolean(KEY_IS_FOODTER, false)
    }

    /**
     * Pa' actualizar el status sin tener que reloguear!
     * (Lo usaremos cuando llene el formulario de 'SignupFoodterActivity')
     */
    fun setFoodterStatus(context: Context, status: Boolean) {
        val editor = getPrefs(context).edit()
        editor.putBoolean(KEY_IS_FOODTER, status)
        editor.apply()
    }



    /**
     * Pa jalar el ID del vato (El "RENO")
     */
    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    /**
     * Pa' jalar el Nombre del vato! (El "Harold Mundo")
     */
    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_NAME, null)
    }

    /**
     * Pa' cuando el vato cierre sesión!
     */
    fun logout(context: Context) {
        val editor = getPrefs(context).edit()
        editor.clear() // ¡'clear()' ya borra todo, incluido el 'is_foodter', perfecto!
        editor.apply()
    }
}