package com.hriportfolio.speaketh.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hriportfolio.speaketh.Contacts;
import com.hriportfolio.speaketh.R;
import com.squareup.picasso.Picasso;


public class RequestsFragment extends Fragment {

    private RecyclerView requestsRecyclerView;
    private View reqView;
    private DatabaseReference reqRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        reqView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        reqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        requestsRecyclerView = reqView.findViewById(R.id.requests_recycler_view);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return reqView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(reqRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestViewHolder holder,
                                                    int position, @NonNull Contacts model) {

                        holder.itemView.findViewById(R.id.req_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.req_reject_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.req_reject_button_for_sent).setVisibility(View.GONE);

                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("received")) {
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {

                                                    final String requesting_user_pic = dataSnapshot.child("image")
                                                            .getValue().toString();

                                                    Picasso.get().load(requesting_user_pic).placeholder(R.drawable.profile_image)
                                                            .into(holder.uImage);
                                                }
                                                final String requesting_user_name = dataSnapshot.child("name")
                                                        .getValue().toString();
                                                final String requesting_user_status = dataSnapshot.child("status")
                                                        .getValue().toString();

                                                holder.uName.setText(requesting_user_name);
                                                holder.uStatus.setText("Wants to connect with you.");
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                    else if(type.equals("sent")){

                                        holder.itemView.findViewById(R.id.req_accept_button).setVisibility(View.GONE);
                                        holder.itemView.findViewById(R.id.req_reject_button).setVisibility(View.GONE);
                                        holder.itemView.findViewById(R.id.req_reject_button_for_sent).setVisibility(View.VISIBLE);

                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {

                                                    final String requesting_user_pic = dataSnapshot.child("image")
                                                            .getValue().toString();

                                                    Picasso.get().load(requesting_user_pic).placeholder(R.drawable.profile_image)
                                                            .into(holder.uImage);
                                                }
                                                final String requesting_user_name = dataSnapshot.child("name")
                                                        .getValue().toString();
                                                final String requesting_user_status = dataSnapshot.child("status")
                                                        .getValue().toString();

                                                holder.uName.setText(requesting_user_name);
                                                holder.uStatus.setText("You have sent a request to "+requesting_user_name);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        holder.itemView.findViewById(R.id.req_accept_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                contactsRef.child(currentUserId).child(list_user_id).child("Contact")
                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            contactsRef.child(list_user_id).child(currentUserId).child("Contact")
                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        reqRef.child(currentUserId).child(list_user_id)
                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    reqRef.child(list_user_id).child(currentUserId)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                Toast.makeText(getContext(),"New Contact Added!",Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                        holder.itemView.findViewById(R.id.req_reject_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                reqRef.child(currentUserId).child(list_user_id)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            reqRef.child(list_user_id).child(currentUserId)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getContext(),"Rejected!!",Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                        holder.itemView.findViewById(R.id.req_reject_button_for_sent).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                reqRef.child(currentUserId).child(list_user_id)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            reqRef.child(list_user_id).child(currentUserId)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getContext(),"Request Removed!",Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.display_users_layout, parent, false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };

        requestsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView uName;
        TextView uStatus;
        CircleImageView uImage;
        Button acceptButton, cancelButton,req_reject_button_for_sent;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            uName = itemView.findViewById(R.id.display_user_name);
            uStatus = itemView.findViewById(R.id.display_user_status);
            uImage = itemView.findViewById(R.id.display_user_profile_image);
            acceptButton = itemView.findViewById(R.id.req_accept_button);
            cancelButton = itemView.findViewById(R.id.req_reject_button);
            req_reject_button_for_sent = itemView.findViewById(R.id.req_reject_button_for_sent);
        }
    }
}
