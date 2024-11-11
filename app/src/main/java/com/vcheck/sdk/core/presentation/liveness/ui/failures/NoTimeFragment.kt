package com.vcheck.sdk.core.presentation.liveness.ui.failures

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentErrorNoTimeBinding
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

class NoTimeFragment : ThemeWrapperFragment() {

    private var _binding: FragmentErrorNoTimeBinding? = null

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.tryAgainButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.noTimeBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.noTimeTitle.setTextColor(Color.parseColor(it))
            _binding!!.noTimeSubtitle.setTextColor(Color.parseColor(it))
            //_binding!!.tryAgainButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.errorColorHex?.let {
            _binding!!.errorImage.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error_no_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentErrorNoTimeBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.tryAgainButton.setOnClickListener {
            findNavController().popBackStack()
            (activity as VCheckLivenessActivity).recreate()
        }
    }
}