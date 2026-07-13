import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAdapter(
    private val musicas: List<Musica>,
    private val onPlayClick: (Musica) -> Unit,
    private val onAddToPlaylist: (Musica) -> Unit
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.tvTitulo)
        val artista: TextView = view.findViewById(R.id.tvArtista)
        val btnPlay: ImageButton = view.findViewById(R.id.btnPlay)
        val btnAdd: ImageButton = view.findViewById(R.id.btnAddPlaylist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_musica, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val musica = musicas[position]
        holder.titulo.text = musica.titulo
        holder.artista.text = musica.artista
        holder.btnPlay.setOnClickListener { onPlayClick(musica) }
        holder.btnAdd.setOnClickListener { onAddToPlaylist(musica) }
    }

    override fun getItemCount() = musicas.size
}
