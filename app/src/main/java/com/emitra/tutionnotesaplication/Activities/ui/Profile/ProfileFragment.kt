package com.emitra.tutionnotesaplication.Activities.ui.Profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.emitra.tutionnotesaplication.Activities.Authentication.LoginActivity
import com.emitra.tutionnotesaplication.Constants
import com.emitra.tutionnotesaplication.CustomDialog
import com.emitra.tutionnotesaplication.Models.UserModel
import com.emitra.tutionnotesaplication.R
import com.emitra.tutionnotesaplication.databinding.FragmentNotificationsBinding


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var storage : FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(layoutInflater)
        val view: View = binding.root

        val pd: CustomDialog = CustomDialog(requireContext())
        pd.show()


        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 101)
        }


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) {
                    try {
                        val model: UserModel? = snapshot.getValue(UserModel::class.java)

                        if (model != null) {
                            pd.dismiss()
                            binding.name.text = model.name
                            binding.email.text = model.email
                            binding.phone.text = model.phone

                            Glide.with(requireContext()).load(model.profile_pic).placeholder(R.drawable.user).into(binding.profileImage)
                        } else {
                            pd.dismiss()
                            Constants.error(requireContext(), "No Data Found")
                        }
                    } catch (e: Exception) {
                        Constants.error(requireContext(), "No Data Found ${e.message}")
                        pd.dismiss()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Constants.error(requireContext(), "Error ${error.message}")
                    pd.dismiss()
                }
            }
        }

        database.reference.child("Users").child(auth.currentUser!!.uid)
            .addValueEventListener(listener)


        binding.logout.setOnClickListener {
            try {
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()

            } catch (e: Exception) {
                Constants.error(requireContext(), "User is not logged : ${e.message}")
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            Glide.with(requireContext()).load(imageUri).into(binding.profileImage)
            val loading = CustomDialog(requireContext())
            loading.show()

            val storageRef = storage.reference.child("E_mitra_profile_pic").child(auth.currentUser!!.uid)
            storageRef.putFile(imageUri!!).addOnCompleteListener{upload->
                if (upload.isSuccessful)
                {
                    storageRef.downloadUrl.addOnSuccessListener{url->
                            val map = HashMap<String,Any>()
                            map.put("profile_pic",url.toString())
                            database.reference.child("Users").child(auth.currentUser!!.uid).updateChildren(map)
                                .addOnCompleteListener{database->
                                    if (database.isSuccessful)
                                    {
                                        Constants.success(requireContext(),"Profile Picture Successfully Updated")
                                        loading.dismiss()
                                    }
                                    else{
                                        Constants.error(requireContext(),"Failed to upload the profile picture")
                                        loading.dismiss()
                                    }
                        }
                    }.addOnFailureListener{
                        Constants.error(requireContext(),"Failed to get the url")
                        loading.dismiss()
                    }
                }
                else
                {
                    Constants.error(requireContext(),"Failed to upload the picture")
                    loading.dismiss()
                }


            }
        }
    }
}