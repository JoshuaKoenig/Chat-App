package com.koenig.chatapp.ui.contactsManager

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    private var _fragBinding: FragmentContactsBinding? = null
    private val fragBinding get() = _fragBinding!!

    private  val contactsViewModel: ContactsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentContactsBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewMyContacts.layoutManager = LinearLayoutManager(activity)

        // TODO: observableContacts

        fragBinding.buttonSearchContacts.setOnClickListener {
            val action = ContactsFragmentDirections.actionContactsFragmentToSearchContactsFragment()
            findNavController().navigate(action)
        }

        return  root
    }

    // TODO
    private fun render(){}
}