package com.vcheck.sdk.core.presentation.doc_photo_auto_parsing.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vcheck.sdk.core.R

class DummySegmentationStartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dummy_segmentation_start, container, false)
    }
}