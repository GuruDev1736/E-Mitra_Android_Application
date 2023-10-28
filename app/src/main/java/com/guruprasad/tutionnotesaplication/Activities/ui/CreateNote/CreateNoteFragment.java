package com.guruprasad.tutionnotesaplication.Activities.ui.CreateNote;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.guruprasad.tutionnotesaplication.Adapters.NotesRecyclerViewAdapter;
import com.guruprasad.tutionnotesaplication.Constants;
import com.guruprasad.tutionnotesaplication.CustomDialog;
import com.guruprasad.tutionnotesaplication.Models.NoteDataModel;
import com.guruprasad.tutionnotesaplication.databinding.FragmentHomeBinding;

public class CreateNoteFragment extends Fragment {

    FirebaseDatabase database;
    FirebaseAuth auth;
    private FragmentHomeBinding binding;
    private NotesRecyclerViewAdapter adapter;
    private CustomDialog pd;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        pd = new CustomDialog(getContext());
        pd.show();

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

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String search = query;
                process_search(search);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String search = newText;
                process_search(search);
                return false;
            }
        });


        return root;
    }


    private void process_search(String search) {

        Query searchQuery = database.getReference().child("Notes").child(auth.getCurrentUser().getUid()).orderByChild("tag").startAt(search).endAt(search + "\uf8ff");

        FirebaseRecyclerOptions<NoteDataModel> searchOptions = new FirebaseRecyclerOptions.Builder<NoteDataModel>()
                .setQuery(searchQuery, NoteDataModel.class)
                .build();

        adapter = new NotesRecyclerViewAdapter(searchOptions, getContext());
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
