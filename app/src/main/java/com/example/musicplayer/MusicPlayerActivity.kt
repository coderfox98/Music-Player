package com.example.musicplayer

import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_music_player.*
import java.text.FieldPosition
import java.util.concurrent.TimeUnit

class MusicPlayerActivity : AppCompatActivity(), ItemClicked {


    override fun itemClicked(position: Int) {
        mediaPlayer?.stop()
        state = false
        this.currPosition = position
        play(position)
    }

    private  var mediaPlayer : MediaPlayer? = null
    private lateinit var musicList : MutableList<Music>
    private lateinit var linearLayoutManager : LinearLayoutManager
    private lateinit var adapter: MusicAdapter
    private var currPosition : Int = 0
    private var state = false
    // if player is playing or stopped

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        musicList = mutableListOf()

        if(Build.VERSION.SDK_INT >= 23){
            checkPermissions()

            fab_play.setOnClickListener {
                play(currPosition)
            }
        }

        fab_next.setOnClickListener {
            mediaPlayer?.stop()
            state = false
            if(currPosition < musicList.size - 1) {
                currPosition += 1
                play(currPosition)
            }

            fab_previous.setOnClickListener {
                mediaPlayer?.stop()
                state = false
                if (currPosition > 0) {
                    currPosition -= 1
                    play(currPosition)
                }

            }

            seek_bar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser) {
                        mediaPlayer?.seekTo(progress*1000)
                    }
                    
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    mediaPlayer?.stop()
                    state = false
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    mediaPlayer?.start()
                    state = true
                }

            })

        }

    }

    private fun play(currPosition: Int) {

        if(!state) {
            fab_play.setImageDrawable(resources.getDrawable(R.drawable.ic_stop, null))
            state = true

            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(this@MusicPlayerActivity, Uri.parse(musicList[currPosition].songUri))
                prepare()
                start()
            }

            val mHandler = Handler()

            this@MusicPlayerActivity.runOnUiThread(object: Runnable{
                override fun run() {
                    val playerPosition = mediaPlayer?.currentPosition!! / 1000
                    val totalDuration = mediaPlayer?.duration!! / 1000

                    seek_bar.max = totalDuration
                    seek_bar.progress = playerPosition

                    past_text_view.text = timerFormat(playerPosition.toLong())

                    remain_text_view.text = timerFormat((totalDuration.toLong() - playerPosition.toLong()))

                    mHandler.postDelayed(this,1000)
                }
            })
        }else {
            mediaPlayer?.stop()
            fab_play.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow, null ))
            state = false
        }


    }

    //00:30
    fun timerFormat(time : Long) : String {

        val result = String.format("%02d:%02d",TimeUnit.SECONDS.toMinutes(time),
            TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time)))

        var convert = ""

        for (i in 0 until result.length) {
            convert += result[i]
        }
        return convert
    }

    private fun readSongs() {
        val selection = MediaStore.Audio.Media.IS_MUSIC
        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
        )

        val cursor : Cursor? = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection,
            null, null)

        while(cursor!!.moveToNext()) {
           musicList.add(Music(cursor.getString(0),cursor.getString(1),cursor.getString(2)))

        }

        cursor.close()

        linearLayoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(musicList,this)

        music_list_recycler_view.layoutManager = linearLayoutManager
        music_list_recycler_view.adapter = adapter
    }

    private fun checkPermissions() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            // Read The songs
            readSongs()

        }else {
            // false -> when user denied or permission is disabled from settings
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"Music Player Needs Access to your files",Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // read songs
                readSongs()
            }else {
                Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }


    }
}
