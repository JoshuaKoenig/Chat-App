package com.koenig.chatapp.ui.friendRequestManager

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.koenig.chatapp.adapters.FriendRequestAdapter
import com.koenig.chatapp.adapters.FriendRequestClickListener
import com.koenig.chatapp.databinding.FragmentFriendRequestBinding
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel
import com.koenig.chatapp.utils.SwipeToAcceptCallback
import com.koenig.chatapp.utils.SwipeToRemoveCallback

class FriendRequestFragment : Fragment(), FriendRequestClickListener {

    private var currentTab = "sentTab"

    private var _fragBinding: FragmentFriendRequestBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val friendRequestViewModel: FriendRequestViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragBinding = FragmentFriendRequestBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewRequests.layoutManager = LinearLayoutManager(activity)
        fragBinding.noContent.text = "No open Requests"

        // ITEM TOUCH HANDLER
        // Swipe to accept friend requests
        val swipeAcceptHandler = object : SwipeToAcceptCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                onAcceptRequest(viewHolder.itemView.tag as ContactModel)
            }
        }
        val itemTouchAcceptHelper = ItemTouchHelper(swipeAcceptHandler)

        // Swipe to reject friend request
        val swipeToReject = object : SwipeToRemoveCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onRejectRequest(viewHolder.itemView.tag as ContactModel)
            }
        }
        val itemTouchRejectHelper = ItemTouchHelper(swipeToReject)

        // Swipe to withdraw sent request
        val swipeToWithdraw = object : SwipeToRemoveCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                withdrawRequest(viewHolder.itemView.tag as ContactModel)
            }
        }
        val itemTouchWithdrawHelper = ItemTouchHelper(swipeToWithdraw)


        itemTouchWithdrawHelper.attachToRecyclerView(fragBinding.recyclerViewRequests)

        fragBinding.tablayout.addOnTabSelectedListener(object  : TabLayout.OnTabSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null)
                {
                    when(tab.contentDescription)
                    {
                        "sentTab" -> {

                            fragBinding.noContent.text = "No open Requests"
                            currentTab = tab.contentDescription.toString()
                            startObservingOpenRequests()
                            itemTouchAcceptHelper.attachToRecyclerView(null)
                            itemTouchRejectHelper.attachToRecyclerView(null)
                            itemTouchWithdrawHelper.attachToRecyclerView(fragBinding.recyclerViewRequests)

                        }
                        "receiveTab" -> {
                            fragBinding.noContent.text = "No received Requests"
                            currentTab = tab.contentDescription.toString()
                            startObservingReceivedRequests()
                            itemTouchAcceptHelper.attachToRecyclerView(fragBinding.recyclerViewRequests)
                            itemTouchRejectHelper.attachToRecyclerView(fragBinding.recyclerViewRequests)
                            itemTouchWithdrawHelper.attachToRecyclerView(null)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        // OBSERVING
        startObservingOpenRequests()

        return root
    }

    private fun renderFriendRequests(currentContacts : List<ContactModel>, mode: String)
    {
       fragBinding.recyclerViewRequests.adapter = FriendRequestAdapter(currentContacts as ArrayList<ContactModel>, mode,this)

        if (currentContacts.isEmpty())
        {
            fragBinding.recyclerViewRequests.visibility = View.GONE
            fragBinding.noContent.visibility = View.VISIBLE
            fragBinding.noRequestImage.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewRequests.visibility = View.VISIBLE
            fragBinding.noContent.visibility = View.GONE
            fragBinding.noRequestImage.visibility = View.GONE
        }
    }

    private fun startObservingOpenRequests()
    {
        // Clear the adapter and set new when tab changed
        renderFriendRequests(arrayListOf(), "")

        fragBinding.progressBar.visibility = View.VISIBLE
        friendRequestViewModel.observableOpenFriendReq.observe(viewLifecycleOwner
        ) { t ->
            if (currentTab == "sentTab") {
                renderFriendRequests(t as ArrayList, "sentTab")
                fragBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun startObservingReceivedRequests()
    {
        // Clear the adapter and set new when tab changed
        renderFriendRequests(arrayListOf(), "")
        fragBinding.progressBar.visibility = View.VISIBLE

        friendRequestViewModel.observableReceivedFriendReq.observe(viewLifecycleOwner
        ) { t ->
            if (currentTab == "receiveTab") {
                renderFriendRequests(t as ArrayList, "receiveTab")
                fragBinding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                friendRequestViewModel.getOpenFriendRequests(firebaseUser.uid)
                friendRequestViewModel.getReceivedFriendRequests(firebaseUser.uid)
                profileViewModel.getProfile(firebaseUser.uid)

                mainHandler.post(object : Runnable {
                    override fun run() {
                        friendRequestViewModel.getReceivedFriendRequests(firebaseUser.uid)
                        mainHandler.postDelayed(this, 7000)
                    }
                })
            }
        }
    }

    override fun onAcceptRequest(addUser: ContactModel) {

        profileViewModel.observableProfile.observe(viewLifecycleOwner) {

            val currentUser = ContactModel()
            currentUser.userId = it.userId
            currentUser.userName = it.userName
            currentUser.status = it.status
            currentUser.email = it.email
            currentUser.photoUri = it.photoUri
            currentUser.hasNewMessage = false
            currentUser.hasConversation = false
            currentUser.recentMessage = MessageModel()
            currentUser.hasAlreadyLiked = false

            friendRequestViewModel.acceptFriendRequest(currentUser, addUser)
            friendRequestViewModel.getReceivedFriendRequests(loggedInViewModel.liveFirebaseUser.value!!.uid)
        }
    }

    override fun onRejectRequest(rejectUser: ContactModel) {
        friendRequestViewModel.rejectFriendRequest(loggedInViewModel.liveFirebaseUser.value!!.uid, rejectUser.userId)
        friendRequestViewModel.getReceivedFriendRequests(loggedInViewModel.liveFirebaseUser.value!!.uid)
    }

    override fun withdrawRequest(withdrawUser: ContactModel) {
        friendRequestViewModel.withdrawRequest(loggedInViewModel.liveFirebaseUser.value!!.uid, withdrawUser.userId)
        friendRequestViewModel.getOpenFriendRequests(loggedInViewModel.liveFirebaseUser.value!!.uid)
    }
}