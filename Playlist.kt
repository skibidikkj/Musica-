data class Playlist(
    val id: Long = 0,
    val nome: String,
    val musicas: MutableList<Musica> = mutableListOf()
)
