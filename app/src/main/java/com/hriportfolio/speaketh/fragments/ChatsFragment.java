package com.hriportfolio.speaketh.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hriportfolio.speaketh.ChatActivity;
import com.hriportfolio.speaketh.Contacts;
import com.hriportfolio.speaketh.R;
import com.squareup.picasso.Picasso;

public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatFragmentRecycler;
    private DatabaseReference chatRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView= inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatFragmentRecycler = privateChatView.findViewById(R.id.chatFragmentRecycler);
        chatFragmentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Contacts model) {
                        final String usersIds = getRef(position).getKey();
                        final String[] retImage = {""};
                        userRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    if(dataSnapshot.hasChild("image")){
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image)
                                                .into(holder.profile_image);
                                    }
                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();


                                    holder.userName.setText(retName);

                                    if(dataSnapshot.child("userState").hasChild("state")){
                                        String  state = dataSnapshot.child("userState")
                                                .child("state").getValue().toString();
                                        String  date = dataSnapshot.child("userState")
                                                .child("date").getValue().toString();
                                        String  time = dataSnapshot.child("userState")
                                                .child("time").getValue().toString();

                                        if(state.equals("online")){
                                            holder.userStatus.setText("online");
                                        }
                                        if(state.equals("offline")){
                                            holder.userStatus.setText("Last Seen : "+date+" "+time);
                                        }

                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(getContext(), ChatActivity.class);
                                            i.putExtra("visiting_user_id",usersIds);
                                            i.putExtra("visiting_user_name",retName);
                                            i.putExtra("visiting_user_img", retImage[0]);
                                            startActivity(i);
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.display_users_layout, parent, false);
                        ChatViewHolder viewHolder = new ChatViewHolder(view);
                        return viewHolder;
                    }
                };

        chatFragmentRecycler.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        TextView userStatus;
        CircleImageView profile_image;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.display_user_name);
            userStatus = itemView.findViewById(R.id.display_user_status);
            profile_image = itemView.findViewById(R.id.display_user_profile_image);
        }
    }
}
