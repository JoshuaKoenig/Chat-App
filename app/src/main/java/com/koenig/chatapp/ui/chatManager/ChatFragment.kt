package com.koenig.chatapp.ui.chatManager

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.R
import com.koenig.chatapp.adapters.ChatAdapter
import com.koenig.chatapp.databinding.FragmentChatBinding
import com.koenig.chatapp.enums.ChatModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.chatOverviewManager.ChatOverviewViewModel
import java.time.Instant

class ChatFragment : Fragment() {

    private var _fragBinding: FragmentChatBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val chatViewModel: ChatViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val chatOverviewViewModel: ChatOverviewViewModel by activityViewModels()
    private val args by navArgs<ChatFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentChatBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewChat.layoutManager = LinearLayoutManager(activity)
        fragBinding.recyclerViewChat.visibility = View.GONE

        when(args.chatMode)
        {
            ChatModes.SINGLECHATMODE -> chatViewModel.observableUser.observe(viewLifecycleOwner) { renderSingleChat() }
            ChatModes.GROUPCHATMODE -> chatViewModel.observableGroup.observe(viewLifecycleOwner) { renderGroupChat() }
        }

        fragBinding.buttonSendMessage.setOnClickListener {

            when(args.chatMode)
            {
                ChatModes.SINGLECHATMODE -> sendSingeMessage()
                ChatModes.GROUPCHATMODE -> sendGroupMessage()
            }
        }

        chatViewModel.observableMessages.observe(viewLifecycleOwner) { messages ->

            messages?.let {
                when (args.chatMode) {
                    ChatModes.SINGLECHATMODE -> {
                        chatOverviewViewModel.removeHasNewMessageFlag(
                            loggedInViewModel.liveFirebaseUser.value!!.uid,
                            args.userModel!!.userId
                        )
                        renderChatAdapter(messages as ArrayList<MessageModel>)
                    }

                    ChatModes.GROUPCHATMODE -> {
                        chatOverviewViewModel.removeHasNewGroupMessageFlag(
                            args.groupModel!!.groupId,
                            loggedInViewModel.liveFirebaseUser.value!!.uid
                        )
                        renderChatAdapter(messages as ArrayList<MessageModel>)
                    }
                }
            }
        }

        return root
    }

    private fun renderSingleChat()
    {
        fragBinding.chatvm = chatViewModel
        (requireActivity() as MainActivity).toolbar.title = chatViewModel.observableUser.value!!.userName
    }

    private fun renderGroupChat()
    {
        fragBinding.chatvm = chatViewModel
        (requireActivity() as MainActivity).toolbar.title = chatViewModel.observableGroup.value!!.groupName
    }

    private fun sendSingeMessage()
    {
        val message = MessageModel()
        message.wasRead = false
        message.fromUserId = loggedInViewModel.liveFirebaseUser.value!!.uid
        message.toUserId = args.userModel!!.userId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            message.timeStamp = Instant.now().toString()
        }
        message.message = fragBinding.textCurrentMessage.text.toString()
        message.fromUserName = loggedInViewModel.liveFirebaseUser.value!!.displayName.toString()
        chatViewModel.sendMessage(message)
        fragBinding.textCurrentMessage.setText("")

        requireActivity().currentFocus?.let {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }


    }

    private fun sendGroupMessage()
    {
        val message = MessageModel()
        message.fromUserId = loggedInViewModel.liveFirebaseUser.value!!.uid
        message.toUserId = args.groupModel!!.groupId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            message.timeStamp = Instant.now().toString()
        }
        message.message = fragBinding.textCurrentMessage.text.toString()
        message.fromUserName = loggedInViewModel.liveFirebaseUser.value!!.displayName.toString()

        val groupMembers: ArrayList<ContactModel> = ArrayList(args.groupModel!!.groupMembers.values)

        chatViewModel.sendGroupMessage(message, groupMembers)
        fragBinding.textCurrentMessage.setText("")
    }

    private fun renderChatAdapter(messages: ArrayList<MessageModel>)
    {
        Log.d("Debug_Messages", messages.size.toString())
        fragBinding.progressBar.visibility = View.GONE
        fragBinding.recyclerViewChat.adapter = ChatAdapter(messages, loggedInViewModel.liveFirebaseUser.value!!.uid, requireContext())

        if (messages.isEmpty())
        {
            fragBinding.recyclerViewChat.visibility = View.GONE
            fragBinding.textNoChat.visibility = View.VISIBLE
            fragBinding.noMessagesImage.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewChat.visibility = View.VISIBLE
            fragBinding.textNoChat.visibility = View.GONE
            fragBinding.noMessagesImage.visibility = View.GONE
            fragBinding.recyclerViewChat.smoothScrollToPosition(messages.size-1)
        }
    }

    override fun onResume() {
        super.onResume()

        when(args.chatMode)
        {
            ChatModes.SINGLECHATMODE ->
            {
                chatViewModel.getSelectedProfile(args.userModel!!.userId)
                loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
                    if (firebaseUser != null) {
                        chatViewModel.lifeFirebaseUser.value = firebaseUser
                        chatViewModel.retrieveMessage(args.userModel!!.userId)
                    }
                }
            }

            ChatModes.GROUPCHATMODE ->
            {
                chatViewModel.getSelectedGroup(args.groupModel!!.groupId)
                loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
                    if (firebaseUser != null) {
                        chatViewModel.lifeFirebaseUser.value = firebaseUser
                        chatViewModel.retrieveGroupMessages(args.groupModel!!.groupId)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        when(args.chatMode)
        {
            ChatModes.SINGLECHATMODE -> menu.findItem(R.id.action_profile).title = "View Profile"
            ChatModes.GROUPCHATMODE -> menu.findItem(R.id.action_profile).title = "View Group"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (args.chatMode == ChatModes.SINGLECHATMODE)
        {
            when(item.itemId)
            {
                R.id.action_profile -> {
                    val action = ChatFragmentDirections.actionChatFragmentToContactProfileFragment(args.userModel!!)
                    findNavController().navigate(action)
                }

                android.R.id.home -> {
                    chatOverviewViewModel.currentTab.postValue(0)
                }
            }
        }
        else if (args.chatMode == ChatModes.GROUPCHATMODE)
        {
            when(item.itemId)
            {
                R.id.action_profile -> {
                    val action = ChatFragmentDirections.actionChatFragmentToGroupProfileFragment(args.groupModel!!)
                    findNavController().navigate(action)
                }

                android.R.id.home -> {
                    chatOverviewViewModel.currentTab.postValue(1)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}