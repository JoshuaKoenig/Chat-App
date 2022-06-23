package com.koenig.chatapp.ui.chatOverviewManager

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.adapters.ChatOverviewAdapter
import com.koenig.chatapp.adapters.ChatOverviewClickListener
import com.koenig.chatapp.databinding.FragmentChatOverviewBinding
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.chatManager.ChatViewModel
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class ChatOverviewFragment : Fragment(), ChatOverviewClickListener {

    private var _fragBinding: FragmentChatOverviewBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val chatOverviewViewModel: ChatOverviewViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentChatOverviewBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewOpenChats.layoutManager = LinearLayoutManager(activity)

        // OBSERVE
        chatOverviewViewModel.observableChatContacts.observe(viewLifecycleOwner){ contacts ->

            val sortedList = ArrayList(contacts.sortedByDescending { Instant.parse(it.recentMessage.timeStamp) })

            renderChatContacts(sortedList as ArrayList<ContactModel>)
            fragBinding.progressBar.visibility = View.GONE
        }

        chatViewModel.observableMessages.observe(viewLifecycleOwner){
            Log.d("Debug", it.toString())
            chatOverviewViewModel.getAllChatContacts(loggedInViewModel.liveFirebaseUser.value!!.uid)
        }

        return root
    }

    private fun renderChatContacts(contacts: ArrayList<ContactModel>)
    {

        fragBinding.recyclerViewOpenChats.adapter = ChatOverviewAdapter(contacts, this)

        if(contacts.isEmpty())
        {
            fragBinding.recyclerViewOpenChats.visibility = View.GONE
            fragBinding.textNoConversations.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewOpenChats.visibility = View.VISIBLE
            fragBinding.textNoConversations.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner){
            chatOverviewViewModel.getAllChatContacts(it.uid)
        }
    }

    override fun onClickOpenChat(selectedUser: ContactModel) {
        chatOverviewViewModel.removeHasNewMessageFlag(loggedInViewModel.liveFirebaseUser.value!!.uid, selectedUser.userId)
        val action = ChatOverviewFragmentDirections.actionChatOverviewFragmentToChatFragment(selectedUser)
        findNavController().navigate(action)
    }
}