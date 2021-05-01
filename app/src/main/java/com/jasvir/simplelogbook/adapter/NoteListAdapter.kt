package com.jasvir.simplelogbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jasvir.simplelogbook.uitls.DateFormatter
import com.jasvir.simplelogbook.R
import com.jasvir.simplelogbook.model.NoteModel

class NoteListAdapter(
    private val noteClickListener:
    ((view: View, note: NoteModel, isDelete: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    private var noteList = emptyList<NoteModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(noteList[position])
    }

    override fun getItemCount(): Int = noteList.size

    fun updateNoteList(note: List<NoteModel>) {
        noteList = note
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        private val tvNote = view.findViewById<TextView>(R.id.tv_note)
        private val tvDate = view.findViewById<TextView>(R.id.tv_date)
        private val ivMove = view.findViewById<ImageView>(R.id.iv_move)


        init {
            view.setOnClickListener {
                noteClickListener?.invoke(it, noteList[adapterPosition],false)
            }

            ivMove.setOnClickListener {
                noteClickListener?.invoke(it, noteList[adapterPosition],true)
            }
        }

        fun bind(note: NoteModel) {
            tvTitle.text = note.title
            tvTitle.visibility = if (note.title.isEmpty()) View.GONE else View.VISIBLE
            // tvNote.text = note.note
            tvDate.text = DateFormatter.formatDate(note.creationDate)
            ivMove.setBackgroundResource(R.drawable.ic_delete_24)
        }
    }
}