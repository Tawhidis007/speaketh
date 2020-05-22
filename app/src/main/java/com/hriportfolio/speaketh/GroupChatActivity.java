package com.hriportfolio.speaketh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hriportfolio.speaketh.Utilities.KeyString;
import com.hriportfolio.speaketh.Utilities.SharedPreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageOfGroupButton;
    private EditText userInputInGroupChat;
    private ScrollView mScrollView;
    private TextView displayTextMsg;
    private CircleImageView sender_pic;


    private String currentGroupName;
    private String currentUserName;
    private String currentUserId;
    private String currentUserPic;
    private String currentDate;
    private String currentTime;

    SharedPreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private DatabaseReference groupNameRef;
    private DatabaseReference groupMessageKeyRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        initializeFields();
        initPref();

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        sendMessageOfGroupButton.setOnClickListener(view -> {
            saveMessageToDB();
            userInputInGroupChat.setText("");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    private void saveMessageToDB() {
        String message = userInputInGroupChat.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if(!TextUtils.isEmpty(message)){
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMessageKey  = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            if(!currentUserPic.equals("")){
                messageInfoMap.put("image",currentUserPic);
            }

            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void initPref() {
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        currentUserId = preferenceManager.getValue(KeyString.UID,"");
        currentUserName = preferenceManager.getValue(KeyString.USER_NAME,"");
        currentUserPic = preferenceManager.getValue(KeyString.PROFILE_PICTURE_URL,"");
    }

    private void initializeFields() {
        mToolbar =  findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageOfGroupButton = findViewById(R.id.sendMessageOfGroup);
        userInputInGroupChat = findViewById(R.id.userInputInGroupChat);
        displayTextMsg = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);
        sender_pic = findViewById(R.id.sender_pro_pic);
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){

                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        String chatImage ="";
        while(iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            chatImage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMsg.append(chatName+" :\n"+chatMessage+"\n"+chatTime+" at "+chatDate+"\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
