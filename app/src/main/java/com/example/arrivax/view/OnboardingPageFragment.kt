package com.example.arrivax.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.arrivax.R

class OnboardingPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageRes = requireArguments().getInt(ARG_IMAGE_RES)
        val titleRes = requireArguments().getInt(ARG_TITLE_RES)
        val descRes = requireArguments().getInt(ARG_DESC_RES)

        view.findViewById<ImageView>(R.id.onboarding_image).setImageResource(imageRes)
        view.findViewById<TextView>(R.id.onboarding_title).setText(titleRes)
        view.findViewById<TextView>(R.id.onboarding_description).setText(descRes)
    }

    companion object {
        private const val ARG_IMAGE_RES = "image_res"
        private const val ARG_TITLE_RES = "title_res"
        private const val ARG_DESC_RES = "desc_res"

        fun newInstance(
            @DrawableRes imageRes: Int,
            @StringRes titleRes: Int,
            @StringRes descRes: Int
        ): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            val args = Bundle()
            args.putInt(ARG_IMAGE_RES, imageRes)
            args.putInt(ARG_TITLE_RES, titleRes)
            args.putInt(ARG_DESC_RES, descRes)
            fragment.arguments = args
            return fragment
        }
    }
}
