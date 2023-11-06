package com.guruprasad.tutionnotesaplication.Activities.ui.Profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.guruprasad.tutionnotesaplication.Activities.Authentication.LoginActivity
import com.guruprasad.tutionnotesaplication.Constants
import com.guruprasad.tutionnotesaplication.CustomDialog
import com.guruprasad.tutionnotesaplication.Models.UserModel
import com.guruprasad.tutionnotesaplication.databinding.FragmentNotificationsBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var userId: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(layoutInflater)
        val view: View = binding.root

        val pd: CustomDialog = CustomDialog(requireContext())
        pd.show()


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                try {
                    val model: UserModel? = snapshot.getValue(UserModel::class.java)

                    if (model != null) {
                        pd.dismiss()
                        binding.name.text = model.name
                        binding.email.text = model.email
                        binding.phone.text = model.phone
                    } else {
                        pd.dismiss()
                        Constants.error(requireContext(), "No Data Found")
                    }
                } catch (e: Exception) {
                    Constants.error(requireContext(), "No Data Found ${e.message}")
                    pd.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Constants.error(requireContext(), "Error ${error.message}")
                pd.dismiss()
            }

        }


        database.reference.child("Users").child(userId.currentUser!!.uid)
            .addValueEventListener(listener)


        binding.logout.setOnClickListener {
            try {
                userId.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()

            } catch (e: Exception) {
                Constants.error(requireContext(), "User is not logged : ${e.message}")
            }
        }

        return view
    }
}