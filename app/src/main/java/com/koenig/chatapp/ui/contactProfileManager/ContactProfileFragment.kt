package com.koenig.chatapp.ui.contactProfileManager

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.koenig.chatapp.databinding.FragmentContactProfileBinding
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class ContactProfileFragment : Fragment() {

    private var _fragBinding: FragmentContactProfileBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val args by navArgs<ContactProfileFragmentArgs>()

    private lateinit var viewModel: ContactProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentContactProfileBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        renderContactProfile()

        return root
    }

    private fun renderContactProfile()
    {
        fragBinding.textContactName.text = args.contactModel.userName
        fragBinding.textContactStatus.text = args.contactModel.status
        fragBinding.textContactMail.text = args.contactModel.email

        if (args.contactModel.photoUri.isNotEmpty())
        {
            Picasso.get().load(args.contactModel.photoUri)
                .resize(300, 300)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(fragBinding.imageContactUser)
        }
    }

    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()

}