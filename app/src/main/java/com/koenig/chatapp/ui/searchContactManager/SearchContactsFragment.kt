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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.koenig.chatapp.adapters.FoundUserAdapter
import com.koenig.chatapp.adapters.FoundUserClickListener
import com.koenig.chatapp.databinding.FragmentSearchContactsBinding
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.friendRequestManager.FriendRequestViewModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel
import com.koenig.chatapp.utils.SwipeToViewCallback

class SearchContactsFragment : Fragment(), FoundUserClickListener {

    private var currentUser = ContactModel()
    private var _fragBinding: FragmentSearchContactsBinding? = null
    private val fragBinding get() = _fragBinding!!
    private  val searchContactsViewModel: SearchContactsViewModel by activityViewModels()
    private val friendRequestViewModel: FriendRequestViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    private val args by navArgs<SearchContactsFragmentArgs>()

    private val currentUsersContactIds = ArrayList<String>()

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
            profileViewModel.observableProfile.observe(viewLifecycleOwner)
            {
                it.contacts.values.forEach { um ->
                    currentUsersContactIds.add(um.userId)
                }
                startObservingSearchContacts(0)
            }
        }

        searchContactsViewModel.currentTabObserver.observe(viewLifecycleOwner){
            if (it != null)
            {
                if(it == 0)
                {
                    startObservingSearchContacts(it)
                    selectSearchTab()

                }
                else if(it == 1)
                {
                    startObservingRecommendations(it)
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

        // ITEM TOUCH HANDLER
        val swipeViewHandler = object : SwipeToViewCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewContactProfile(viewHolder.itemView.tag as UserModel)
            }
        }
        val itemTouchViewHelper = ItemTouchHelper(swipeViewHandler)
        itemTouchViewHelper.attachToRecyclerView(fragBinding.recyclerViewFoundContacts)

        return root
    }


    private fun render(userList: ArrayList<UserModel>, currentUsersContactIds: ArrayList<String>, currentTab: Int)
    {
        fragBinding.recyclerViewFoundContacts.adapter = FoundUserAdapter(userList, this, currentUsersContactIds, currentTab)

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

    private fun startObservingSearchContacts(currentTab: Int)
    {
        render(arrayListOf(), arrayListOf(), currentTab)
        fragBinding.progressBar.visibility = View.VISIBLE

        searchContactsViewModel.observableUserList.observe(viewLifecycleOwner) { users ->
            users?.let {
                render(users as ArrayList<UserModel>, currentUsersContactIds, currentTab)
                fragBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun startObservingRecommendations(currentTab: Int)
    {
        render(arrayListOf(), arrayListOf(), currentTab)
        fragBinding.progressBar.visibility = View.VISIBLE

        searchContactsViewModel.observableRecUserList.observe(viewLifecycleOwner){
            it?.let {
                render(it as ArrayList<UserModel>, currentUsersContactIds, currentTab)
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

    private fun viewContactProfile(user: UserModel)
    {
        val contact = ContactModel()
        contact.userId = user.userId
        contact.userName = user.userName
        contact.photoUri = user.photoUri
        contact.email = user.email
        contact.status = user.status

        val action = SearchContactsFragmentDirections.actionSearchContactsFragmentToContactProfileFragment(contact, false)
        findNavController().navigate(action)
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

            findNavController().navigateUp()
            Snackbar.make(fragBinding.root, "Friend request sent to ${addUser.userName}", Snackbar.LENGTH_SHORT).show()
        }
    }
}