import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "playlists.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT UNIQUE NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE playlist_musicas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                playlist_id INTEGER,
                musica_id INTEGER,
                titulo TEXT,
                artista TEXT,
                caminho TEXT,
                FOREIGN KEY(playlist_id) REFERENCES playlists(id)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun salvarPlaylist(nome: String, musicas: List<Musica>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply { put("nome", nome) }
            val playlistId = db.insert("playlists", null, cv)

            musicas.forEach { musica ->
                val cvMusica = ContentValues().apply {
                    put("playlist_id", playlistId)
                    put("musica_id", musica.id)
                    put("titulo", musica.titulo)
                    put("artista", musica.artista)
                    put("caminho", musica.caminho)
                }
                db.insert("playlist_musicas", null, cvMusica)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listarPlaylists(): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        val db = readableDatabase
        val cursor = db.query("playlists", null, null, null, null, null, null)
        
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            playlists.add(Playlist(id, nome, listarMusicasPlaylist(id)))
        }
        cursor.close()
        return playlists
    }

    private fun listarMusicasPlaylist(playlistId: Long): MutableList<Musica> {
        val musicas = mutableListOf<Musica>()
        val db = readableDatabase
        val cursor = db.query("playlist_musicas", null, "playlist_id = ?", arrayOf(playlistId.toString()), null, null, null)
        
        while (cursor.moveToNext()) {
            val musica = Musica(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("musica_id")),
                titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo")),
                artista = cursor.getString(cursor.getColumnIndexOrThrow("artista")),
                caminho = cursor.getString(cursor.getColumnIndexOrThrow("caminho"))
            )
            musicas.add(musica)
        }
        cursor.close()
        return musicas
    }

    fun deletarPlaylist(id: Long) {
        val db = writableDatabase
        db.delete("playlist_musicas", "playlist_id = ?", arrayOf(id.toString()))
        db.delete("playlists", "id = ?", arrayOf(id.toString()))
    }
}
