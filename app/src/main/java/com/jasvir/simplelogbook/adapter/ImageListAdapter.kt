package com.jasvir.simplelogbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jasvir.simplelogbook.R

class ImageListAdapter(
    private val noteClickListener:
    ((view: View, position: Int) -> Unit)? = null
) : RecyclerView.Adapter<ImageListAdapter.NoteViewHolder>() {

    private var imageList = emptyList<String>()
    private var isSmall: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_add_image, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(imageList[position], isSmall)
    }

    override fun getItemCount(): Int = imageList.size

    fun updateImageList(imagList: List<String>) {
        imageList = imagList
        notifyDataSetChanged()
    }

    fun updateSmall() {
        isSmall = true
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivImage = view.findViewById<ImageView>(R.id.ivImage)
        private val ivDelete = view.findViewById<ImageView>(R.id.ivDelete)

        init {
            ivDelete.setOnClickListener {
                noteClickListener?.invoke(it, adapterPosition)
            }
        }

        fun bind(image: String, isSmall: Boolean) {
            Glide.with(ivImage.context).load(image).into(ivImage)
            if (isSmall) {
                ivDelete.visibility = View.GONE
                val lp = ivImage.layoutParams as (ConstraintLayout.LayoutParams)
                val size = ivImage.context.resources.getDimension(R.dimen.small_img_height).toInt()
                lp.width = size
                lp.height = size
                ivImage.setLayoutParams(lp)
            }
        }
    }
}