package com.koenig.chatapp.ui.friendRequestManager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.koenig.chatapp.adapters.FriendRequestAdapter
import com.koenig.chatapp.adapters.FriendRequestClickListener
import com.koenig.chatapp.databinding.FragmentFriendRequestBinding
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel

class FriendRequestFragment : Fragment(), FriendRequestClickListener {

    private var currentTab = "sentTab"

    private var _fragBinding: FragmentFriendRequestBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val friendRequestViewModel: FriendRequestViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentFriendRequestBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewRequests.layoutManager = LinearLayoutManager(activity)
        fragBinding.noContent.text = "No open Requests"

        fragBinding.tablayout.addOnTabSelectedListener(object  : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null)
                {
                    when(tab.contentDescription)
                    {
                        "sentTab" -> {

                            fragBinding.noContent.text = "No open Requests"
                            currentTab = tab.contentDescription.toString()
                            startObservingOpenRequests()
                        }
                        "receiveTab" -> {
                            fragBinding.noContent.text = "No received Requests"
                            currentTab = tab.contentDescription.toString()
                            startObservingReceivedRequests()
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
        }
        else
        {
            fragBinding.recyclerViewRequests.visibility = View.VISIBLE
            fragBinding.noContent.visibility = View.GONE
        }
    }

    private fun startObservingOpenRequests()
    {
        // Clear the adapter and set new when tab changed
        renderFriendRequests(arrayListOf(), "")

        fragBinding.progressBar.visibility = View.VISIBLE
        friendRequestViewModel.observableOpenFriendReq.observe(viewLifecycleOwner, object : Observer<List<ContactModel>> {
            override fun onChanged(t: List<ContactModel>?) {

                if (currentTab == "sentTab")
                {
                    renderFriendRequests(t as ArrayList, "sentTab")
                    fragBinding.progressBar.visibility = View.GONE
                }

                friendRequestViewModel.observableOpenFriendReq.removeObserver(this)
            }
        })
    }

    private fun startObservingReceivedRequests()
    {
        // Clear the adapter and set new when tab changed
        renderFriendRequests(arrayListOf(), "")
        fragBinding.progressBar.visibility = View.VISIBLE

        friendRequestViewModel.observableReceivedFriendReq.observe(viewLifecycleOwner, object : Observer<List<ContactModel>> {
            override fun onChanged(t: List<ContactModel>?) {

                if (currentTab == "receiveTab")
                {
                    renderFriendRequests(t as ArrayList, "receiveTab")
                    fragBinding.progressBar.visibility = View.GONE
                }

                friendRequestViewModel.observableReceivedFriendReq.removeObserver(this)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null)
            {
                friendRequestViewModel.getOpenFriendRequests(firebaseUser.uid)
                friendRequestViewModel.getReceivedFriendRequests(firebaseUser.uid)
                profileViewModel.getProfile(firebaseUser.uid)
            }
        })
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

            friendRequestViewModel.acceptFriendRequest(currentUser, addUser)
            startObservingReceivedRequests()
        }
    }

    override fun onRejectRequest(rejectUser: ContactModel) {
        friendRequestViewModel.rejectFriendRequest(loggedInViewModel.liveFirebaseUser.value!!.uid, rejectUser.userId)
    }

    override fun withdrawRequest(withdrawUser: ContactModel) {
        friendRequestViewModel.withdrawRequest(loggedInViewModel.liveFirebaseUser.value!!.uid, withdrawUser.userId)
    }
}