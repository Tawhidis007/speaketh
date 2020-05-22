package com.hriportfolio.speaketh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.Refresher {


    private String messageReceiverId, messageReceiverName, messageReceiverImage;
    private TextView receiverName, receiverLastSeen;
    private CircleImageView receiverImage;
    private ImageView back_from_chat_button;
    private ImageButton sendMessagePrivateChat, sendFilesButton;
    private EditText userInputInPrivateChat;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private String messageSenderId;
    private DatabaseReference rootRef;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter adapter;
    private RecyclerView messageListRecyclerView;

    String saveCurrentTime, saveCurrentDate;
    private String checker = "";
    private String url = "";
    private String otherUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverId = getIntent().getExtras().get("visiting_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visiting_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visiting_user_img").toString();

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();


        initControllers();

        receiverName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(receiverImage);

        back_from_chat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendMessagePrivateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg();
            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF Files",
                        "Docs"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File type");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent,"Select Image"),0);

                        }
                        if(i==1){
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(Intent.createChooser(intent,"Select PDF File"),0);
                        }
                        if(i==2){
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(Intent.createChooser(intent,"Select DOC File"),0);
                        }
                    }
                });
                builder.show();
            }
        });

        displayChat();
    }

    public void displayChat(){
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messageList.add(messages);
                        //Log.d("msg_check","size of msg list : "+messageList.size());
                        adapter.notifyDataSetChanged();

                        messageListRecyclerView.scrollToPosition(messageListRecyclerView.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            fileUri = data.getData();
            if(!checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId)
                        .child(messageReceiverId).push();
                String msgPushId = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(msgPushId+"."+checker);

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            task.getException().printStackTrace();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            otherUrl = downloadUrl.toString();

                            Map picMessageMap = new HashMap();
                            picMessageMap.put("message", otherUrl);
                            picMessageMap.put("name", fileUri.getLastPathSegment());
                            picMessageMap.put("type", checker);
                            picMessageMap.put("from", messageSenderId);
                            picMessageMap.put("to", messageReceiverId);
                            picMessageMap.put("messageID", msgPushId);
                            picMessageMap.put("time", saveCurrentTime);
                            picMessageMap.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + msgPushId, picMessageMap);
                            messageBodyDetails.put(messageReceiverRef + "/" + msgPushId, picMessageMap);


                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    userInputInPrivateChat.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else if(checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId)
                        .child(messageReceiverId).push();
                String msgPushId = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(msgPushId+"."+"jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            task.getException().printStackTrace();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            url = downloadUrl.toString();

                            Map picMessageMap = new HashMap();
                            picMessageMap.put("message", url);
                            picMessageMap.put("name", fileUri.getLastPathSegment());
                            picMessageMap.put("type", checker);
                            picMessageMap.put("from", messageSenderId);
                            picMessageMap.put("to", messageReceiverId);
                            picMessageMap.put("messageID", msgPushId);
                            picMessageMap.put("time", saveCurrentTime);
                            picMessageMap.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + msgPushId, picMessageMap);
                            messageBodyDetails.put(messageReceiverRef + "/" + msgPushId, picMessageMap);


                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {

                                    }
                                    userInputInPrivateChat.setText("");
                                }
                            });
                        }
                    }
                });
            }else{
                Toast.makeText(ChatActivity.this,"Nothing selected!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initControllers() {


        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        receiverName = findViewById(R.id.receiverName);
        receiverLastSeen = findViewById(R.id.receiverLastSeen);
        receiverImage = findViewById(R.id.receiver_image);
        back_from_chat_button = findViewById(R.id.back_from_chat_button);
        sendMessagePrivateChat = findViewById(R.id.sendMessagePrivateChat);
        sendFilesButton = findViewById(R.id.send_files_button);
        userInputInPrivateChat = findViewById(R.id.userInputInPrivateChat);

        adapter = new MessageAdapter(messageList,this);
        messageListRecyclerView = findViewById(R.id.privateMessagesRecycler);
        linearLayoutManager = new LinearLayoutManager(this);
        messageListRecyclerView.setLayoutManager(linearLayoutManager);
        messageListRecyclerView.setAdapter(adapter);
        displayLastSeen();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


    }

    private void displayLastSeen() {
        rootRef.child("Users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState")
                                    .child("state").getValue().toString();
                            String date = dataSnapshot.child("userState")
                                    .child("date").getValue().toString();
                            String time = dataSnapshot.child("userState")
                                    .child("time").getValue().toString();

                            if (state.equals("online")) {
                                receiverLastSeen.setText("online");
                            }
                            if (state.equals("offline")) {
                                receiverLastSeen.setText("Last Seen : " + date + " " + time);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void sendMsg() {
        String messageText = userInputInPrivateChat.getText().toString();
        if (!TextUtils.isEmpty(messageText)) {
            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();
            String msgPushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageReceiverId);
            messageTextBody.put("messageID", msgPushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + msgPushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + msgPushId, messageTextBody);


            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                    }
                    userInputInPrivateChat.setText("");
                }
            });
        }

    }

    @Override
    public void refreshAdapter() {
        displayChat();
    }
}
