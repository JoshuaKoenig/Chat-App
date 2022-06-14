package com.koenig.chatapp.ui.searchContactManager

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.FragmentProfileBinding
import com.koenig.chatapp.databinding.FragmentSearchContactsBinding
import com.koenig.chatapp.ui.profileManager.ProfileViewModel

class SearchContactsFragment : Fragment() {

    private var _fragBinding: FragmentSearchContactsBinding? = null
    private val fragBinding get() = _fragBinding!!
    private  val searchContactsViewModel: SearchContactsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentSearchContactsBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        return root
    }

}