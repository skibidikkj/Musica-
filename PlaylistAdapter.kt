import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onPlayClick: (Playlist) -> Unit,
    private val onDeleteClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.tvPlaylistNome)
        val quantidade: TextView = view.findViewById(R.id.tvPlaylistQuantidade)
        val btnPlay: ImageButton = view.findViewById(R.id.btnPlayPlaylist)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeletePlaylist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.nome.text = playlist.nome
        holder.quantidade.text = "${playlist.musicas.size} músicas"
        holder.btnPlay.setOnClickListener { onPlayClick(playlist) }
        holder.btnDelete.setOnClickListener { onDeleteClick(playlist) }
    }

    override fun getItemCount() = playlists.size
}
