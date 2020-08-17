package com.example.chatappfinal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatappfinal.presentation.loadPhoto
import kotlinx.android.synthetic.main.fragment_photo_viewer.*

class PhotoViewerFragment : Fragment() {

    private val url by lazy { PhotoViewerFragmentArgs.fromBundle(requireArguments()).url }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_photo_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo_imageView.loadPhoto(url)
    }
}