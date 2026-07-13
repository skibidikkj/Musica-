import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var playlistAdapter: PlaylistAdapter
    private var todasMusicas = mutableListOf<Musica>()
    private var playlists = mutableListOf<Playlist>()
    private var mediaPlayer: MediaPlayer? = null
    private var playlistAtual: MutableList<Musica> = mutableListOf()
    private var indiceAtual = 0

    companion object {
        private const val PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        // Verifica permissões
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST)
        } else {
            carregarMusicas()
        }

        setupTabs()
        setupFab()
    }

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val recyclerMusicas = findViewById<RecyclerView>(R.id.recyclerMusicas)
        val recyclerPlaylists = findViewById<RecyclerView>(R.id.recyclerPlaylists)

        // Configura Recycler de músicas
        musicAdapter = MusicAdapter(todasMusicas,
            { musica -> tocarMusica(musica) },
            { musica -> mostrarDialogAdicionarPlaylist(musica) }
        )
        recyclerMusicas.layoutManager = LinearLayoutManager(this)
        recyclerMusicas.adapter = musicAdapter

        // Configura Recycler de playlists
        playlistAdapter = PlaylistAdapter(playlists,
            { playlist -> tocarPlaylist(playlist) },
            { playlist -> deletarPlaylist(playlist) }
        )
        recyclerPlaylists.layoutManager = LinearLayoutManager(this)
        recyclerPlaylists.adapter = playlistAdapter

        // Alterna entre abas
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        recyclerMusicas.visibility = android.view.View.VISIBLE
                        recyclerPlaylists.visibility = android.view.View.GONE
                    }
                    1 -> {
                        recyclerMusicas.visibility = android.view.View.GONE
                        recyclerPlaylists.visibility = android.view.View.VISIBLE
                        carregarPlaylists()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun carregarMusicas() {
        todasMusicas.clear()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null, null, MediaStore.Audio.Media.TITLE
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                todasMusicas.add(Musica(
                    id = it.getLong(idCol),
                    titulo = it.getString(titleCol) ?: "Desconhecido",
                    artista = it.getString(artistCol) ?: "Desconhecido",
                    caminho = it.getString(dataCol) ?: "",
                    duracao = it.getLong(durationCol)
                ))
            }
        }
        musicAdapter.notifyDataSetChanged()
        Toast.makeText(this, "${todasMusicas.size} músicas carregadas", Toast.LENGTH_SHORT).show()
    }

    private fun carregarPlaylists() {
        playlists = db.listarPlaylists()
        playlistAdapter.notifyDataSetChanged()
    }

    private fun mostrarDialogAdicionarPlaylist(musica: Musica) {
        val input = EditText(this)
        input.hint = "Nome da playlist"

        AlertDialog.Builder(this)
            .setTitle("Adicionar à playlist")
            .setView(input)
            .setPositiveButton("Criar nova") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty()) {
                    db.salvarPlaylist(nome, listOf(musica))
                    carregarPlaylists()
                    Toast.makeText(this, "Playlist '$nome' criada!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun tocarMusica(musica: Musica) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(musica.caminho)
                prepare()
                start()
            }
            Toast.makeText(this, "Tocando: ${musica.titulo}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Erro ao tocar música", Toast.LENGTH_SHORT).show()
        }
    }

    private fun tocarPlaylist(playlist: Playlist) {
        if (playlist.musicas.isEmpty()) {
            Toast.makeText(this, "Playlist vazia!", Toast.LENGTH_SHORT).show()
            return
        }
        playlistAtual = playlist.musicas.toMutableList()
        indiceAtual = 0
        tocarMusica(playlistAtual[indiceAtual])
    }

    private fun deletarPlaylist(playlist: Playlist) {
        AlertDialog.Builder(this)
            .setTitle("Deletar playlist")
            .setMessage("Deseja remover '${playlist.nome}'?")
            .setPositiveButton("Sim") { _, _ ->
                db.deletarPlaylist(playlist.id)
                carregarPlaylists()
                Toast.makeText(this, "Playlist removida!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupFab() {
        findViewById<Button>(R.id.btnProxima).setOnClickListener {
            if (playlistAtual.isNotEmpty() && indiceAtual < playlistAtual.size - 1) {
                indiceAtual++
                tocarMusica(playlistAtual[indiceAtual])
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            carregarMusicas()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
