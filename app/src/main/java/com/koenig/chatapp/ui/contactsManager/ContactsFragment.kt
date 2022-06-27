package com.koenig.chatapp.ui.contactsManager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.adapters.ContactsAdapter
import com.koenig.chatapp.adapters.ContactsClickListener
import com.koenig.chatapp.databinding.FragmentContactsBinding
import com.koenig.chatapp.enums.ChatModes
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel


class ContactsFragment : Fragment(), ContactsClickListener {

    private var _fragBinding: FragmentContactsBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val contactsViewModel: ContactsViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    private val args by navArgs<ContactsFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentContactsBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewMyContacts.layoutManager = LinearLayoutManager(activity)

        contactsViewModel.observableContacts.observe(viewLifecycleOwner, Observer { contacts ->
            contacts?.let {
                render(contacts as ArrayList<ContactModel>)
                fragBinding.progressBar.visibility = View.GONE
            }
        })

        fragBinding.buttonSearchContacts.setOnClickListener {
            val action = ContactsFragmentDirections.actionContactsFragmentToSearchContactsFragment()
            findNavController().navigate(action)
        }

        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                contactsViewModel.loadAllContacts(firebaseUser.uid)
            }
        }

        // MODES => default: false
        if (args.contactClickModes == ContactClickModes.CREATEGROUPMODE || args.contactClickModes == ContactClickModes.ADDCONTACTMODE)
        {
            renderSelectMode()
        }

        return  root
    }

    private fun render(contacts: ArrayList<ContactModel>)
    {
        fragBinding.recyclerViewMyContacts.adapter = ContactsAdapter(contacts, this, args.contactClickModes)

        if(contacts.isEmpty())
        {
            fragBinding.recyclerViewMyContacts.visibility = View.GONE
            fragBinding.textNoContacts.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewMyContacts.visibility = View.VISIBLE
            fragBinding.textNoContacts.visibility = View.GONE
        }
    }

    private fun renderSelectMode()
    {
        fragBinding.buttonSearchContacts.visibility = View.GONE
    }

    override fun onClickOpenChat(selectedUser: ContactModel) {
        val action = ContactsFragmentDirections.actionContactsFragmentToChatFragment(ChatModes.SINGLECHATMODE, selectedUser, null)
        findNavController().navigate(action)
    }

    override fun onClickSelectUser(selectedUser: ContactModel) {
        val action = ContactsFragmentDirections.actionContactsFragmentToCreateGroupChatFragment(selectedUser)
        findNavController().navigate(action)
    }

    override fun onClickAddUserToGroup(selectedUser: ContactModel) {
        val action = ContactsFragmentDirections.actionContactsFragmentToGroupProfileFragment(args.groupModel, selectedUser)
        findNavController().navigate(action)
    }
}