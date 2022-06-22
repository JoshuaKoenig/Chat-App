package com.koenig.chatapp.ui.chatManager

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.R
import com.koenig.chatapp.adapters.ChatAdapter
import com.koenig.chatapp.databinding.FragmentChatBinding
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import okhttp3.internal.Internal.instance
import java.time.format.DateTimeFormatter

class ChatFragment : Fragment() {

    private var _fragBinding: FragmentChatBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val chatViewModel: ChatViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val args by navArgs<ChatFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentChatBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewChat.layoutManager = LinearLayoutManager(activity)

        chatViewModel.observableUser.observe(viewLifecycleOwner, Observer { render() })

        fragBinding.buttonSendMessage.setOnClickListener {
            val message = MessageModel()
            message.fromUserId = loggedInViewModel.liveFirebaseUser.value!!.uid
            message.toUserId = args.userModel.userId
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                message.timeStamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").toString()
            }
            message.message = fragBinding.textCurrentMessage.text.toString()
            chatViewModel.sendMessage(message)
        }

        chatViewModel.observableMessages.observe(viewLifecycleOwner, Observer { messages ->

            messages?.let {

                renderChatAdapter(messages as ArrayList<MessageModel>)
            }

        })

        return root
    }

    private fun render()
    {
        fragBinding.chatvm = chatViewModel
        (requireActivity() as MainActivity).toolbar.title = chatViewModel.observableUser.value!!.userName
    }

    private fun renderChatAdapter(messages: ArrayList<MessageModel>)
    {
        fragBinding.progressBar.visibility = View.GONE
        fragBinding.recyclerViewChat.visibility = View.VISIBLE
        fragBinding.recyclerViewChat.adapter = ChatAdapter(messages, loggedInViewModel.liveFirebaseUser.value!!.uid, requireContext())

        if (messages.isEmpty())
        {
            fragBinding.recyclerViewChat.visibility = View.GONE
            fragBinding.textNoChat.visibility = View.VISIBLE

        }
        else
        {
            fragBinding.recyclerViewChat.visibility = View.VISIBLE
            fragBinding.textNoChat.visibility = View.GONE

            fragBinding.recyclerViewChat.smoothScrollToPosition(messages.size-1)
        }
    }

    override fun onResume() {
        super.onResume()
        chatViewModel.getSelectedProfile(args.userModel.userId)
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null)
            {
                chatViewModel.lifeFirebaseUser.value = firebaseUser

               chatViewModel.retrieveMessage(args.userModel.userId)
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.action_profile -> {
                val action = ChatFragmentDirections.actionChatFragmentToContactProfileFragment(args.userModel)
                findNavController().navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}