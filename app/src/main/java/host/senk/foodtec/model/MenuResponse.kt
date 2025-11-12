package host.senk.foodtec.model

// El "sobre" que nos manda obtenerMenu.php
// Tiene que coincidir con el JSON: {"status": "...", "menu": [...]}
data class MenuResponse(
    val status: String,
    val menu: List<ComidaItem> // ¡Una lista de los moldes que ya hicimos!
)
