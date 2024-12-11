package com.cs407.gymroyale

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class ProfilePictureAdapter(
    private val context: Context,
    private val profilePictures: List<Int>
) : BaseAdapter() {

    override fun getCount(): Int = profilePictures.size

    override fun getItem(position: Int): Int = profilePictures[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_profile_picture, parent, false
        )
        val imageView = view.findViewById<ImageView>(R.id.imageViewProfilePicture)
        imageView.setImageResource(profilePictures[position])
        return view
    }
}
