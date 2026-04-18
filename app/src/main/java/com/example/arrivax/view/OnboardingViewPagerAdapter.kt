package com.example.arrivax.view

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.arrivax.R

// Data class to hold the content for each onboarding page
private data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int
)

class OnboardingViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    // A list containing the data for all onboarding screens
    private val onboardingPages = listOf(
        OnboardingPage(
            R.drawable.onboarding_screen_1,
            R.string.onboarding_title_1,
            R.string.onboarding_desc_1
        ),
        OnboardingPage(
            R.drawable.onboarding_screen_2,
            R.string.onboarding_title_2,
            R.string.onboarding_desc_2
        ),
        OnboardingPage(
            R.drawable.onboarding_screen_3,
            R.string.onboarding_title_3,
            R.string.onboarding_desc_3
        )
    )

    override fun getItemCount(): Int = onboardingPages.size

    override fun createFragment(position: Int): Fragment {
        val page = onboardingPages[position]
        return OnboardingPageFragment.newInstance(
            page.imageRes,
            page.titleRes,
            page.descRes
        )
    }
}
