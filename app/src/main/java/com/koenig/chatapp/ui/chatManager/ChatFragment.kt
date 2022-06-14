package com.koenig.chatapp.ui.chatManager

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _fragBinding: FragmentChatBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val chatViewModel: ChatViewModel by activityViewModels()

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

        return root
    }

}