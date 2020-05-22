package com.hriportfolio.speaketh.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hriportfolio.speaketh.Contacts;
import com.hriportfolio.speaketh.R;
import com.squareup.picasso.Picasso;

public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView contactsRecyclerView;
    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsRecyclerView = contactsView.findViewById(R.id.contacts_recycler_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new
                FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position,
                                                    @NonNull Contacts model) {

                        String userIds = getRef(position).getKey();
                        usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){

                                    if(dataSnapshot.child("userState").hasChild("state")){
                                        String  state = dataSnapshot.child("userState")
                                                .child("state").getValue().toString();
                                        String  date = dataSnapshot.child("userState")
                                                .child("date").getValue().toString();
                                        String  time = dataSnapshot.child("userState")
                                                .child("time").getValue().toString();

                                        if(state.equals("online")){
                                            holder.onlineIcon.setVisibility(View.VISIBLE);
                                        }
                                        if(state.equals("offline")){
                                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    if (dataSnapshot.hasChild("image")) {
                                        String proPic = dataSnapshot.child("image").getValue().toString();
                                        String name = dataSnapshot.child("name").getValue().toString();
                                        String status = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(name);
                                        holder.userStatus.setText(status);
                                        Picasso.get().load(proPic).placeholder(R.drawable.profile_image)
                                                .into(holder.profile_image);
                                    } else {
                                        String name = dataSnapshot.child("name").getValue().toString();
                                        String status = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(name);
                                        holder.userStatus.setText(status);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.display_users_layout, parent, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;

                    }
                };
        contactsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userStatus;
        CircleImageView profile_image;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.display_user_name);
            userStatus = itemView.findViewById(R.id.display_user_status);
            profile_image = itemView.findViewById(R.id.display_user_profile_image);
            onlineIcon = itemView.findViewById(R.id.display_user_online);
        }
    }
}
