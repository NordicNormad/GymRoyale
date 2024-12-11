package com.cs407.gymroyale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.DialogFragment

class ProfilePictureDialog(
    private val onPictureSelected: (Int) -> Unit
) : DialogFragment() {

    private val profilePictures = listOf(
        R.drawable.profile_1,
        R.drawable.profile_2,
        R.drawable.profile_3,
        R.drawable.profile_4,
        R.drawable.profile_5
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_select_profile_picture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridView: GridView = view.findViewById(R.id.gridProfilePictures)
        val adapter = ProfilePictureAdapter(requireContext(), profilePictures)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            onPictureSelected(profilePictures[position])
            dismiss()
        }
    }
}
