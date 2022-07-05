package com.koenig.chatapp.ui.contactsManager

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.R
import com.koenig.chatapp.adapters.ContactsAdapter
import com.koenig.chatapp.adapters.ContactsClickListener
import com.koenig.chatapp.databinding.FragmentContactsBinding
import com.koenig.chatapp.enums.ChatModes
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.GroupModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.utils.SwipeToViewCallback

class ContactsFragment : Fragment(), ContactsClickListener {

    private var _fragBinding: FragmentContactsBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val contactsViewModel: ContactsViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    private val args by navArgs<ContactsFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
            val currentContactIds = ArrayList<String>()

            contactsViewModel.contacts.value!!.forEach {
                currentContactIds.add(it.userId)
            }

            val action = ContactsFragmentDirections.actionContactsFragmentToSearchContactsFragment(currentContactIds.toTypedArray())
            findNavController().navigate(action)
        }

        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {

                if (args.contactClickModes == ContactClickModes.ADDCONTACTMODE || args.contactClickModes == ContactClickModes.CREATEGROUPMODE)
                {
                    // Users who are already in the group cannot be added again.
                    val groupContactIds = ArrayList<String>()
                    args.groupModel!!.groupMembers.forEach {
                        groupContactIds.add(it.value.userId)
                    }

                    contactsViewModel.loadAllContacts(firebaseUser.uid, true, groupContactIds)
                }
                else
                {
                    contactsViewModel.loadAllContacts(firebaseUser.uid, false, null)
                }
            }
        }

        // MODES => default: false
        if (args.contactClickModes == ContactClickModes.CREATEGROUPMODE || args.contactClickModes == ContactClickModes.ADDCONTACTMODE)
        {
            renderSelectMode()
        }

        // ITEM TOUCH HANDLER
        if(args.contactClickModes == ContactClickModes.DEFAULTMODE) {
            val swipeViewHandler = object : SwipeToViewCallback(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {


                    viewContactProfile(viewHolder.itemView.tag as ContactModel)

                }
            }
            val itemTouchViewHelper = ItemTouchHelper(swipeViewHandler)
            itemTouchViewHelper.attachToRecyclerView(fragBinding.recyclerViewMyContacts)
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
            fragBinding.noContactsImage.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewMyContacts.visibility = View.VISIBLE
            fragBinding.textNoContacts.visibility = View.GONE
            fragBinding.noContactsImage.visibility = View.GONE
        }
    }

    private fun renderSelectMode()
    {
        (requireActivity() as MainActivity).toolbar.title = "Select Contact"
        fragBinding.buttonSearchContacts.visibility = View.GONE
    }

    // OVERRIDES
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            android.R.id.home -> {

                if (args.contactClickModes == ContactClickModes.ADDCONTACTMODE)
                {
                    val action = ContactsFragmentDirections.actionContactsFragmentToGroupProfileFragment(args.groupModel, null, true)
                    findNavController().navigate(action)
                }
                else if(args.contactClickModes == ContactClickModes.CREATEGROUPMODE)
                {
                    findNavController().navigateUp()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun viewContactProfile(contactModel: ContactModel)
    {
        val action = ContactsFragmentDirections.actionContactsFragmentToContactProfileFragment(contactModel)
        findNavController().navigate(action)
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