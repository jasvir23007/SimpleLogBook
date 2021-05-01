package com.jasvir.simplelogbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.jasvir.simplelogbook.uitls.DateFormatter
import com.jasvir.simplelogbook.R
import com.jasvir.simplelogbook.model.SubNotes

class NoteSubListAdapter(
    private val noteClickListener:
    ((view: View, position: Int, isMove: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<NoteSubListAdapter.NoteViewHolder>() {

    private var noteList = emptyList<SubNotes>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(noteList[position])
    }

    override fun getItemCount(): Int = noteList.size

    fun updateNoteList(note: List<SubNotes>) {
        noteList = note
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        private val tvNote = view.findViewById<TextView>(R.id.tv_note)
        private val tvDate = view.findViewById<TextView>(R.id.tv_date)
        private val ivMove = view.findViewById<AppCompatImageView>(R.id.iv_move)
        private val rvImage = view.findViewById<RecyclerView>(R.id.rv_images)


        init {
            view.setOnClickListener {
                noteClickListener?.invoke(it, adapterPosition, false)
            }
            ivMove.setOnClickListener {
                noteClickListener?.invoke(it, adapterPosition, true)

            }
        }

        fun bind(note: SubNotes) {
            tvTitle.text = note.title
            tvTitle.visibility = if (note.title.isEmpty()) View.GONE else View.VISIBLE
            tvNote.text = note.description
            tvDate.text = DateFormatter.formatDate(note.creationDate)
            val imageListAdapter = ImageListAdapter { _, position -> }
            imageListAdapter.updateImageList(note.imgPath)
            imageListAdapter.updateSmall()

            rvImage.adapter = imageListAdapter
        }
    }
}