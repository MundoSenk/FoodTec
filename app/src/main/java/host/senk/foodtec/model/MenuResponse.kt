package host.senk.foodtec.model

// El "sobre" que nos manda obtenerMenu.php
data class MenuResponse(
    val status: String,

    // --- ¡¡EL TRUCO, PA!! ---
    // Le decimos que la lista "menu" PUEDE ser nula (con el ?)
    // (Porque si falla, el PHP no la va a mandar)
    val menu: List<ComidaItem>?,

    // ¡Y le decimos que el "mensaje" TAMBIÉN puede ser nulo!
    // (Porque si SÍ jala, el PHP no manda el mensaje)
    val mensaje: String?
)
