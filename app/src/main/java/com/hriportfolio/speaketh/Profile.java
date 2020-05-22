package com.hriportfolio.speaketh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class Profile extends AppCompatActivity {

    private String receiverUserId, currentState, senderUserId;

    @BindView(R.id.visiting_profile_image)
    CircleImageView visiting_profile_image;
    @BindView(R.id.visiting_profile_user_name)
    TextView visiting_profile_user_name;
    @BindView(R.id.visiting_profile_user_status)
    TextView visiting_profile_user_status;
    @BindView(R.id.send_message_request_button)
    Button send_message_request_button;
    @BindView(R.id.reject_request_button)
    Button reject_request_button;

    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private DatabaseReference chatReqRef;
    private DatabaseReference contactsRef;
    private DatabaseReference notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mAuth = FirebaseAuth.getInstance();

        receiverUserId = getIntent().getExtras().get("idToVisit").toString();
        currentState = "new";
        senderUserId = mAuth.getCurrentUser().getUid();


        retrieveUserInfo();

    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && dataSnapshot.hasChild("image")) {
                    String img = dataSnapshot.child("image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(img).placeholder(R.drawable.profile_image).into(visiting_profile_image);
                    visiting_profile_user_name.setText(name);
                    visiting_profile_user_status.setText(status);

                    manageChatRequest();
                } else {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();

                    visiting_profile_user_name.setText(name);
                    visiting_profile_user_status.setText(status);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {

        chatReqRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)) {
                            String request_type = dataSnapshot.child(receiverUserId)
                                    .child("request_type").getValue().toString();
                            if (request_type.equals("sent")) {
                                currentState = "request_sent";
                                send_message_request_button.setText("Cancel Request");
                            } else if (request_type.equals("received")) {
                                currentState = "request_received";
                                send_message_request_button.setText("Accept Request");
                                reject_request_button.setVisibility(View.VISIBLE);
                                reject_request_button.setEnabled(true);
                                reject_request_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelChatRequest();
                                    }
                                });

                            }
                        } else {
                            contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiverUserId)) {
                                        currentState = "friends";
                                        send_message_request_button.setText("Remove Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if (!senderUserId.equals(receiverUserId)) {
            send_message_request_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    send_message_request_button.setEnabled(false);
                    if (currentState.equals("new")) {
                        sendChatRequest();
                    }
                    if (currentState.equals("request_sent")) {
                        cancelChatRequest();
                    }
                    if (currentState.equals("request_received")) {
                        acceptRequest();
                    }
                    if (currentState.equals("friends")) {
                        removeSpecificContact();
                    }
                }
            });
        } else {
            send_message_request_button.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                send_message_request_button.setEnabled(true);
                                                currentState = "new";
                                                send_message_request_button.setText("Send Request");
                                                reject_request_button.setVisibility(View.INVISIBLE);
                                                reject_request_button.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptRequest() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                chatReqRef.child(senderUserId).child(receiverUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    chatReqRef.child(receiverUserId).child(senderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        send_message_request_button.setEnabled(true);
                                                                                        currentState = "friends";
                                                                                        send_message_request_button.setText("Remove Contact");

                                                                                        reject_request_button.setVisibility(View.INVISIBLE);
                                                                                        reject_request_button.setEnabled(false);

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

    private void cancelChatRequest() {
        chatReqRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatReqRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                send_message_request_button.setEnabled(true);
                                                currentState = "new";
                                                send_message_request_button.setText("Send Request");
                                                reject_request_button.setVisibility(View.INVISIBLE);
                                                reject_request_button.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        chatReqRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatReqRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        HashMap<String,String> chatNotificationMap = new HashMap<>();
                                        chatNotificationMap.put("from",senderUserId);
                                        chatNotificationMap.put("type","request");
                                        notificationRef.child(receiverUserId).push()
                                                .setValue(chatNotificationMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            send_message_request_button.setEnabled(true);
                                                            currentState = "request_sent";
                                                            send_message_request_button.setText("Cancel Request");
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
