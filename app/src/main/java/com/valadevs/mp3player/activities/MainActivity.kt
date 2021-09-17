package com.valadevs.mp3player.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.valadevs.mp3player.R
import com.valadevs.mp3player.models.Song


class MainActivity : AppCompatActivity(), Listener {

    lateinit var player: SimpleExoPlayer
    lateinit var playerView: PlayerControlView;
    var database = FirebaseDatabase.getInstance()
    var myRef = database.getReference("songs")
    lateinit var mediaItemList:ArrayList<MediaItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaItemList = ArrayList()
        getSongs()
    }

    private fun getSongs() {

        myRef.get().addOnSuccessListener {

            for(data in it.children){
                var auxSong = data.getValue(Song::class.java)
                if(auxSong!=null){

                    var metadata = MediaMetadata.Builder()
                        .setTitle(auxSong.title)
                        .setArtist(auxSong.artist)
                        .setAlbumTitle(auxSong.album)
                        .setArtworkUri(Uri.parse(auxSong.img))
                        .build()

                    var item:MediaItem = MediaItem.Builder()
                        .setUri(auxSong.song_url)
                        .setMediaMetadata(metadata)
                        .build()

                    mediaItemList.add(item)
                }
            }

            initPlayer()


        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun initPlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        playerView = findViewById(R.id.player)
        val param = PlaybackParameters(1f)
        player.playbackParameters = param
        player.addListener(this)
        player.repeatMode = Player.REPEAT_MODE_ALL
        playerView.player = player
        player.addMediaItems(mediaItemList);
        player.prepare()
    }


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)

        var albumImage:ImageView = findViewById(R.id.album_image)
        var title:TextView = findViewById(R.id.title)
        var album:TextView = findViewById(R.id.album)
        var artist:TextView = findViewById(R.id.artist)

        if (mediaItem != null) {
            var metaData = mediaItem.mediaMetadata
            Picasso.get().load(metaData.artworkUri).into(albumImage);
            title.text = metaData.title
            album.text = metaData.albumTitle
            artist.text = metaData.artist
        }
    }
}