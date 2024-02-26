package com.emitra.tutionnotesaplication.Activities.ui.CreateNote;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.emitra.tutionnotesaplication.Activities.ui.Profile.ProfileFragment;
import com.emitra.tutionnotesaplication.Models.UserModel;
import com.emitra.tutionnotesaplication.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.emitra.tutionnotesaplication.Adapters.NotesRecyclerViewAdapter;
import com.emitra.tutionnotesaplication.Constants;
import com.emitra.tutionnotesaplication.CustomDialog;
import com.emitra.tutionnotesaplication.Models.NoteDataModel;
import com.emitra.tutionnotesaplication.databinding.FragmentHomeBinding;
import com.google.firebase.database.ValueEventListener;

public class CreateNoteFragment extends Fragment {

    FirebaseDatabase database;
    FirebaseAuth auth;
    private FragmentHomeBinding binding;
    private NotesRecyclerViewAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        CustomDialog pd = new CustomDialog(requireContext());
        pd.show();

        database.getReference().child("Users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    UserModel data = snapshot.getValue(UserModel.class);
                    if (data!=null)
                    {
                        pd.dismiss();
                        Glide.with(getContext()).load(data.getProfile_pic()).placeholder(R.drawable.user).into(binding.circleImageView);
                        binding.circleImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Navigation.findNavController(v).navigate(R.id.navigation_profile);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Constants.error(getContext(),"Failed to get the user data");
                pd.dismiss();
            }
        });




        binding.create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), CreateNoteActivity.class));
            }
        });

        binding.recyclerview.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Query baseQuery = database.getReference().child("Notes").child(auth.getCurrentUser().getUid());
        FirebaseRecyclerOptions<NoteDataModel> options = new FirebaseRecyclerOptions.Builder<NoteDataModel>()
                .setQuery(baseQuery, NoteDataModel.class)
                .build();


        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String search = query;
                process_search(search, pd);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String search = newText;
                process_search(search, pd);
                return false;
            }
        });


        adapter = new NotesRecyclerViewAdapter(options, getContext()) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                pd.dismiss();
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                Constants.error(getContext(), "Error : " + error.getMessage());
                pd.dismiss();
            }
        };

        binding.recyclerview.setAdapter(adapter);


        return root;
    }


    private void process_search(String search, CustomDialog pd) {

        Query searchQuery = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).orderByChild("tag").startAt(search).endAt(search + "\uf8ff");

        FirebaseRecyclerOptions<NoteDataModel> searchOptions = new FirebaseRecyclerOptions.Builder<NoteDataModel>()
                .setQuery(searchQuery, NoteDataModel.class)
                .build();

        adapter = new NotesRecyclerViewAdapter(searchOptions, getContext()) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                pd.dismiss();

            }


            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                pd.dismiss();
            }
        };
        adapter.startListening();
        binding.recyclerview.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
