package com.guruprasad.tutionnotesaplication.Activities.ui.AskMe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.guruprasad.tutionnotesaplication.databinding.FragmentNotificationsBinding

class AskMeFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentNotificationsBinding.inflate(layoutInflater)
        val view:View = binding.root

    return view
    }
}