package com.koenig.chatapp.ui.searchContactManager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.adapters.FoundUserAdapter
import com.koenig.chatapp.adapters.FoundUserClickListener
import com.koenig.chatapp.databinding.FragmentSearchContactsBinding
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel

class SearchContactsFragment : Fragment(), FoundUserClickListener {

    private var _fragBinding: FragmentSearchContactsBinding? = null
    private val fragBinding get() = _fragBinding!!
    private  val searchContactsViewModel: SearchContactsViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentSearchContactsBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        fragBinding.recyclerViewFoundContacts.layoutManager = LinearLayoutManager(activity)

        searchContactsViewModel.observableUserList.observe(viewLifecycleOwner, Observer { users ->
            users?.let {
                render(users as ArrayList<UserModel>)
            }
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
        }
        else
        {
            fragBinding.recyclerViewFoundContacts.visibility = View.VISIBLE
            fragBinding.usersNotFound.visibility = View.GONE
        }
    }

    override fun onUserAddClick(addUser: UserModel) {
        searchContactsViewModel.addContact(loggedInViewModel.liveFirebaseUser.value!!.uid, addUser)
    }

}