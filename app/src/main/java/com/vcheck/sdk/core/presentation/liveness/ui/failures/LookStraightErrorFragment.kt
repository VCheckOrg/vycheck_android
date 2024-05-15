package com.vcheck.sdk.core.presentation.liveness.ui.failures

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vcheck.sdk.core.R
import com.vcheck.sdk.core.VCheckSDK
import com.vcheck.sdk.core.databinding.FragmentErrorLookStraightBinding
import com.vcheck.sdk.core.presentation.liveness.VCheckLivenessActivity
import com.vcheck.sdk.core.util.utils.ThemeWrapperFragment

class LookStraightErrorFragment : ThemeWrapperFragment() {

    private var _binding: FragmentErrorLookStraightBinding? = null

    private val args: LookStraightErrorFragmentArgs by navArgs()

    override fun changeColorsToCustomIfPresent() {
        VCheckSDK.designConfig!!.primary?.let {
            _binding!!.lookStraightErrorButton.setBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundPrimaryColorHex?.let {
            _binding!!.lookStraightErrorBackground.background = ColorDrawable(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.backgroundSecondaryColorHex?.let {
            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.primaryTextColorHex?.let {
            _binding!!.lookStraightErrorTitle.setTextColor(Color.parseColor(it))
            _binding!!.lookStraightErrorSubtitle.setTextColor(Color.parseColor(it))
            //_binding!!.lookStraightErrorButton.setTextColor(Color.parseColor(it))
        }
        VCheckSDK.designConfig!!.errorColorHex?.let {
            _binding!!.errorImage.setColorFilter(Color.parseColor(it))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error_look_straight, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentErrorLookStraightBinding.bind(view)

        changeColorsToCustomIfPresent()

        requireActivity().onBackPressedDispatcher.addCallback {
            //Stub; no back press needed here
        }

        _binding!!.lookStraightErrorButton.setOnClickListener {
            if (args.isFromUploadResponse) {
                findNavController().popBackStack()
            }
            findNavController().popBackStack()
            (activity as VCheckLivenessActivity).recreate()
        }
    }
}