package com.koenig.chatapp.ui.aboutManager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.koenig.chatapp.databinding.FragmentAboutBinding
import hotchemi.android.rate.AppRate

class AboutFragment : Fragment() {

    private var _fragBinding: FragmentAboutBinding? = null
    private val fragBinding get() = _fragBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentAboutBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.RateThisApp.setOnClickListener{
            AppRate.with(requireContext()).showRateDialog(requireActivity())
        }

        fragBinding.ContactUs.setOnClickListener {

            val mailIntent = Intent(Intent.ACTION_SEND)
            mailIntent.data = Uri.parse("mailto:")
            mailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("joshua.koenig@live.com"))
            startActivity(Intent.createChooser(mailIntent, "Choose Email Client..."))
        }

        return root
    }

}