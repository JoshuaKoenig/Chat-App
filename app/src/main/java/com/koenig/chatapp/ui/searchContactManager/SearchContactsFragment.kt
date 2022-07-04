package com.koenig.chatapp.ui.searchContactManager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.koenig.chatapp.adapters.FoundUserAdapter
import com.koenig.chatapp.adapters.FoundUserClickListener
import com.koenig.chatapp.databinding.FragmentSearchContactsBinding
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.friendRequestManager.FriendRequestViewModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel

class SearchContactsFragment : Fragment(), FoundUserClickListener {

    private var currentUser = ContactModel()
    private var _fragBinding: FragmentSearchContactsBinding? = null
    private val fragBinding get() = _fragBinding!!
    private  val searchContactsViewModel: SearchContactsViewModel by activityViewModels()
    private val friendRequestViewModel: FriendRequestViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    private val args by navArgs<SearchContactsFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentSearchContactsBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerViewFoundContacts.layoutManager = LinearLayoutManager(activity)


        // OBSERVERS
        if(searchContactsViewModel.currentTab.value == null)
        {
            startObservingSearchContacts()
        }

        searchContactsViewModel.currentTabObserver.observe(viewLifecycleOwner){
            if (it != null)
            {
                if(it == 0)
                {
                    startObservingSearchContacts()
                    selectSearchTab()

                }
                else if(it == 1)
                {
                    startObservingRecommendations()
                    selectRecommendationTab()
                }
            }
        }

        // TEXT CHANGE LISTENER
        fragBinding.editSearchContact.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchContactsViewModel.getFilteredUsers(
                    loggedInViewModel.liveFirebaseUser.value!!.uid,
                    args.contactIds.toList() as ArrayList<String>,
                    p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        // TAB LAYOUT
        fragBinding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                if(tab != null)
                {
                    when(tab.contentDescription)
                    {
                        "searchTab" -> {
                            searchContactsViewModel.currentTab.postValue(0)
                            fragBinding.recommendedCard.visibility = View.GONE
                            fragBinding.searchContactLayout.visibility = View.VISIBLE

                        }
                        "recommendedTab" -> {
                            searchContactsViewModel.currentTab.postValue(1)
                            fragBinding.recommendedCard.visibility = View.VISIBLE
                            fragBinding.searchContactLayout.visibility = View.GONE
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return root
    }


    private fun render(userList: ArrayList<UserModel>)
    {
        fragBinding.recyclerViewFoundContacts.adapter = FoundUserAdapter(userList, this)

        if(userList.isEmpty())
        {
            fragBinding.recyclerViewFoundContacts.visibility = View.GONE
            fragBinding.usersNotFound.visibility = View.VISIBLE
            fragBinding.noUsersFoundImage.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.recyclerViewFoundContacts.visibility = View.VISIBLE
            fragBinding.usersNotFound.visibility = View.GONE
            fragBinding.noUsersFoundImage.visibility = View.GONE
        }
    }

    private fun startObservingSearchContacts()
    {
        render(arrayListOf())
        fragBinding.progressBar.visibility = View.VISIBLE

        searchContactsViewModel.observableUserList.observe(viewLifecycleOwner, Observer { users ->
            users?.let {
                render(users as ArrayList<UserModel>)
                fragBinding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun startObservingRecommendations()
    {
        render(arrayListOf())
        fragBinding.progressBar.visibility = View.VISIBLE

        searchContactsViewModel.observableRecUserList.observe(viewLifecycleOwner){
            it?.let {
                render(it as ArrayList<UserModel>)
                fragBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun selectSearchTab()
    {
        fragBinding.tablayout.getTabAt(0)!!.select()
    }

    private fun selectRecommendationTab()
    {
        fragBinding.tablayout.getTabAt(1)!!.select()
    }


    // OVERRIDES
    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) {
            profileViewModel.getProfile(it.uid)
            searchContactsViewModel.getFilteredUsers(it.uid, ArrayList(args.contactIds.toList()), "")
            searchContactsViewModel.getRecommendedContacts(it.uid,  ArrayList(args.contactIds.toList()))
        }
    }

    override fun onUserAddClick(addUser: ContactModel) {

        profileViewModel.observableProfile.observe(viewLifecycleOwner) {
            currentUser.userId = it.userId
            currentUser.userName = it.userName
            currentUser.email = it.email
            currentUser.photoUri = it.photoUri
            currentUser.status = it.status
            friendRequestViewModel.sendFriendRequest(addUser,currentUser)
        }
    }
}