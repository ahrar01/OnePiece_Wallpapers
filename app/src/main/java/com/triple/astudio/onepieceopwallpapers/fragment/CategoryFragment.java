package com.triple.astudio.onepieceopwallpapers.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.SimpleOnSearchActionListener;
import com.triple.astudio.onepieceopwallpapers.R;
import com.triple.astudio.onepieceopwallpapers.adapter.CategoriesAdapter;
import com.triple.astudio.onepieceopwallpapers.models.Category;

import java.util.ArrayList;
import java.util.List;


public class CategoryFragment extends Fragment {

    private MaterialSearchBar searchBar;
    private List<Category> categoryList;
    private ProgressBar progressBar;
    private DatabaseReference dbCategories;
    private RecyclerView recyclerView;
    private CategoriesAdapter adapter, duplicateAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        categoryList = new ArrayList<>();
        adapter = new CategoriesAdapter(getActivity(), categoryList);
        duplicateAdapter = new CategoriesAdapter(getActivity(), categoryList);
        recyclerView.setAdapter(adapter);
        searchBar = view.findViewById(R.id.searchBar);
        searchBar.setHint("Luffy...");
        searchBar.setOnSearchActionListener(new SimpleOnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                super.onSearchStateChanged(enabled);

                if (!enabled) {

                    recyclerView.setAdapter(duplicateAdapter);
                } else
                    recyclerView.setAdapter(adapter);

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                super.onSearchConfirmed(text);
                adapter.getFilter().filter(text);   // filtering the result
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                super.onButtonClicked(buttonCode);

            }
        });

        dbCategories = FirebaseDatabase.getInstance().getReference("categories");

        dbCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        String desc = ds.child("desc").getValue(String.class);
                        String thumb = ds.child("thumbnail").getValue(String.class);
                        Category c = new Category(name, desc, thumb);
                        if (name.equals("new")) {

                        } else {
                            categoryList.add(c);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    duplicateAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



}