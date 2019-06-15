package com.example.musicplayer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MusicAdapter(private var musicList: MutableList<Music>, private var itemClicked : ItemClicked) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): MusicViewHolder {

        val context = viewGroup.context
        val inflater = LayoutInflater.from(context)
        val shouldAttactToParentImmediately = false

        val view = inflater.inflate(R.layout.music_items, viewGroup, shouldAttactToParentImmediately)

        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = musicList[position]

        holder.bindMusic(item)

    }


    inner class MusicViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view : View = v
        private lateinit var music : Music
        private var artistName : TextView
        private var songName : TextView

        init {
            artistName = view.findViewById(R.id.artist_text_view)
            songName = view.findViewById(R.id.song_text_view)

            view.setOnClickListener(this)
        }

        fun bindMusic(music: Music) {
            this.music = music

            artistName.text = music.artistName
            songName.text = music.songName
        }

        override fun onClick(v: View?) {
            itemClicked.itemClicked(adapterPosition)
        }

    }

}