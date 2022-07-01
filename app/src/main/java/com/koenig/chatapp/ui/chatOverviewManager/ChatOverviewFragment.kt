package com.koenig.chatapp.ui.chatOverviewManager

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.koenig.chatapp.adapters.ChatOverviewAdapter
import com.koenig.chatapp.adapters.ChatOverviewClickListener
import com.koenig.chatapp.adapters.GroupChatAdapter
import com.koenig.chatapp.adapters.GroupChatListener
import com.koenig.chatapp.databinding.FragmentChatOverviewBinding
import com.koenig.chatapp.enums.ChatModes
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.GroupModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.chatManager.ChatViewModel
import com.koenig.chatapp.utils.SwipeToViewCallback
import java.time.Instant
import java.util.*

class ChatOverviewFragment : Fragment(), ChatOverviewClickListener, GroupChatListener {

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
        fragBinding.addGroupChat.visibility = View.GONE

        // TABS
        fragBinding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                if(tab != null)
                {
                    when(tab.contentDescription)
                    {
                        "chatTab" -> {
                            chatOverviewViewModel.currentTab.postValue(0)
                            fragBinding.addGroupChat.visibility = View.GONE
                            fragBinding.buttonContacts.visibility = View.VISIBLE

                        }
                        "groupTab" -> {
                            chatOverviewViewModel.currentTab.postValue(1)
                            fragBinding.addGroupChat.visibility = View.VISIBLE
                            fragBinding.buttonContacts.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {

            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {

            }
        })

        // CLICK LISTENERS
        fragBinding.addGroupChat.setOnClickListener {
            val action = ChatOverviewFragmentDirections.actionChatOverviewFragmentToCreateGroupChatFragment()
            findNavController().navigate(action)
        }

        fragBinding.buttonContacts.setOnClickListener {
            val action = ChatOverviewFragmentDirections.actionChatOverviewFragmentToContactsFragment(ContactClickModes.DEFAULTMODE, null)
            findNavController().navigate(action)
        }

        // OBSERVE
        if(chatOverviewViewModel.currentTab.value == null)
        {
            startObservingChats()
        }

        chatOverviewViewModel.currentTabObserver.observe(viewLifecycleOwner){
            if (it != null)
            {
                if(it == 0)
                {
                    startObservingChats()
                    selectSingleChatTab()

                }
                else if(it == 1)
                {
                    startObservingGroupChats()
                    selectGroupChatTab()
                }
            }
        }

        // ITEM TOUCH HANDLER
        val swipeViewHandler = object : SwipeToViewCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                if(viewHolder.itemView.tag.toString().substring(0, 12) == "ContactModel")
                {
                    viewContactProfile(viewHolder.itemView.tag as ContactModel)
                }
                else if(viewHolder.itemView.tag.toString().substring(0, 10) == "GroupModel")
                {
                    viewGroupProfile(viewHolder.itemView.tag as GroupModel)
                }
            }

        }
        val itemTouchViewHelper = ItemTouchHelper(swipeViewHandler)
            itemTouchViewHelper.attachToRecyclerView(fragBinding.recyclerViewOpenChats)

        return root
    }

    private fun renderChatContacts(contacts: ArrayList<ContactModel>)
    {
        fragBinding.recyclerViewOpenChats.adapter = ChatOverviewAdapter(contacts, this)

        if(contacts.isEmpty())
        {
            fragBinding.recyclerViewOpenChats.visibility = View.GONE
            fragBinding.textNoConversations.text = "No Chats yet."
            fragBinding.textNoConversations.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewOpenChats.visibility = View.VISIBLE
            fragBinding.textNoConversations.visibility = View.GONE
        }
    }

    private fun renderGroupChats(groupChats: ArrayList<GroupModel>)
    {
        fragBinding.recyclerViewOpenChats.adapter = GroupChatAdapter(groupChats, this, loggedInViewModel.liveFirebaseUser.value!!.uid)

        if(groupChats.isEmpty())
        {
            fragBinding.recyclerViewOpenChats.visibility = View.GONE
            fragBinding.textNoConversations.text = "No Groups yet."
            fragBinding.textNoConversations.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewOpenChats.visibility = View.VISIBLE
            fragBinding.textNoConversations.visibility = View.GONE
        }
    }

    private fun startObservingChats()
    {
        renderChatContacts(arrayListOf())
        fragBinding.progressBar.visibility = View.VISIBLE
        chatOverviewViewModel.observableChatContacts.observe(viewLifecycleOwner){ contacts ->

            val sortedList = ArrayList(contacts.sortedByDescending { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Instant.parse(it.recentMessage.timeStamp)
            } else {
                TODO("VERSION.SDK_INT < O")
                }
            })

            renderChatContacts(sortedList as ArrayList<ContactModel>)
            fragBinding.progressBar.visibility = View.GONE
        }
    }

    private fun startObservingGroupChats()
    {
        renderGroupChats(arrayListOf())
        fragBinding.progressBar.visibility = View.VISIBLE
        chatOverviewViewModel.observableGroupChats.observe(viewLifecycleOwner){groups ->

            val sortedGroups = ArrayList(groups.sortedByDescending { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Instant.parse(it.recentMessage.timeStamp)
            } else {
                TODO("VERSION.SDK_INT < O")
                }
            })

            renderGroupChats(sortedGroups as ArrayList<GroupModel>)
            fragBinding.progressBar.visibility = View.GONE
        }

        // To update chats instantly => Not working perfect yet
        chatViewModel.observableMessages.observe(viewLifecycleOwner){
            chatOverviewViewModel.getAllGroupChats(loggedInViewModel.liveFirebaseUser.value!!.uid)
        }
    }

    private fun selectSingleChatTab(){
        fragBinding.tablayout.getTabAt(0)!!.select()
    }

    private fun selectGroupChatTab(){
        fragBinding.tablayout.getTabAt(1)!!.select()
    }

    private fun viewContactProfile(contact: ContactModel)
    {
        val action = ChatOverviewFragmentDirections.actionChatOverviewFragmentToContactProfileFragment(contact)
        findNavController().navigate(action)
    }

    private fun viewGroupProfile(group: GroupModel)
    {
        val action = ChatOverviewFragmentDirections.actionChatOverviewFragmentToGroupProfileFragment(group)
        findNavController().navigate(action)
    }

    // OVERRIDES
    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner){
            chatOverviewViewModel.getAllChatContacts(it.uid)
            chatOverviewViewModel.getAllGroupChats(it.uid)
        }
    }

    override fun onClickOpenChat(selectedUser: ContactModel) {
        chatOverviewViewModel.removeHasNewMessageFlag(loggedInViewModel.liveFirebaseUser.value!!.uid, selectedUser.userId)
        val action = ChatOverviewFragmentDirections.actionChatOverviewFragmentToChatFragment(ChatModes.SINGLECHATMODE, selectedUser, null)
        findNavController().navigate(action)
    }

    override fun onClickOpenGroupChat(selectedGroupChat: GroupModel) {
        chatOverviewViewModel.removeHasNewGroupMessageFlag(selectedGroupChat.groupId, loggedInViewModel.liveFirebaseUser.value!!.uid)
        val action = ChatOverviewFragmentDirections.actionChatOverviewFragmentToChatFragment(ChatModes.GROUPCHATMODE, null, selectedGroupChat)
        findNavController().navigate(action)
    }
}