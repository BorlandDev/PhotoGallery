package com.bignerdranch.android.photogallery.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.api.GalleryItem
import com.bignerdranch.android.photogallery.databinding.ListItemGalleyBinding

class PhotoListAdapter(
    private val onItemClicked: (Uri) -> Unit
) :
    ListAdapter<GalleryItem, PhotoListAdapter.PhotoViewHolder>(PhotosDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGalleyBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    class PhotoViewHolder(
        private val binding: ListItemGalleyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(galleryItem: GalleryItem, onItemClicked: (Uri) -> Unit) {
            binding.imageView.load(galleryItem.url) {
                placeholder(R.drawable.bill_up_close)
                crossfade(true)
            }
            binding.root.setOnClickListener { onItemClicked(galleryItem.photoPageUri) }
        }
    }

    object PhotosDiffCallback : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean =
            oldItem.id == newItem.id
    }
}