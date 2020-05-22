package com.hriportfolio.speaketh.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hriportfolio.speaketh.GroupAdapter;
import com.hriportfolio.speaketh.GroupChatActivity;
import com.hriportfolio.speaketh.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View groupsFragmentView;
    private ListView listView;
    //private ArrayAdapter<String> arrayAdapter;
    private GroupAdapter arrayAdapter;
    private ArrayList<String> groupList = new ArrayList<>();

    private DatabaseReference groupRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupsFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        initializeFields();
        retrieveAndDisplayGroups();
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String currentGroupName = adapterView.getItemAtPosition(position).toString();
            Intent i = new Intent(getContext(), GroupChatActivity.class);
            i.putExtra("groupName",currentGroupName);
            startActivity(i);
        });

        return groupsFragmentView;
    }

    private void retrieveAndDisplayGroups() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());

                }
                groupList.clear();
                groupList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFields() {
        listView = groupsFragmentView.findViewById(R.id.groups_list);
        arrayAdapter = new GroupAdapter(getContext(),
                R.layout.custom_group_listview,groupList);
        listView.setAdapter(arrayAdapter);
    }

}
