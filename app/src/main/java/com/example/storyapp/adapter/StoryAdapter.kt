package com.example.storyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.databinding.ItemStoryBinding
import com.example.storyapp.models.Story

class StoryAdapter(
    private val stories: List<Story>,
    private val onItemClickListener: (Story) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story, onItemClickListener: (Story) -> Unit) {
            binding.tvStoryTitle.text = story.name
            Glide.with(binding.root.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivStoryThumbnail)

            // Set click listener
            binding.root.setOnClickListener {
                onItemClickListener(story)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position], onItemClickListener)
    }

    override fun getItemCount(): Int = stories.size
}