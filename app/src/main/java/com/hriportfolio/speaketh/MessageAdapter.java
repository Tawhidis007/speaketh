package com.hriportfolio.speaketh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {



    private static Refresher refresher;

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessagesList,Refresher refresh) {
        this.userMessagesList = userMessagesList;
        refresher = refresh;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout
                , parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.from;
        String fromMessageType = messages.type;

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image)
                            .into(holder.receiverProfileImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);

        holder.messageSenderImageView.setVisibility(View.GONE);
        holder.messageReceiverImageView.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {


            if (fromUserId.equals(messageSenderId)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.message + "\n \n" + messages.time + " - " + messages.date);
            } else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.message + "\n \n" + messages.time + " - " + messages.date);

            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.messageSenderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.message).into(holder.messageSenderImageView);
            } else {
                holder.messageReceiverImageView.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.message).into(holder.messageReceiverImageView);
            }

        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.messageSenderImageView.setVisibility(View.VISIBLE);
                holder.messageSenderImageView.setBackgroundResource(R.drawable.ic_insert_drive_file_black_24dp);

            } else {
                holder.messageReceiverImageView.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImageView.setBackgroundResource(R.drawable.ic_insert_drive_file_black_24dp);

            }
        }

        if (fromUserId.equals(messageSenderId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).type.equals("pdf") || userMessagesList.get(position).type.equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Download and View This Document",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position,holder);
                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(userMessagesList.get(position).message));
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 3) {
                                    deleteMessageForAll(position,holder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).type.equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position,holder);
                                } else if (i == 2) {
                                    deleteMessageForAll(position,holder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).type.equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "View This Image",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position,holder);
                                } else if (i == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).message);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 3) {
                                    deleteMessageForAll(position,holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).type.equals("pdf") || userMessagesList.get(position).type.equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Download and View This Document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteReceivedMessage(position,holder);
                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(userMessagesList.get(position).message));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).type.equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteReceivedMessage(position,holder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).type.equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "View This Image",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteReceivedMessage(position,holder);
//                                    Intent intent = new Intent(holder.itemView.getContext(),MainActivity.class);
//                                    holder.itemView.getContext().startActivity(intent);

                                } else if (i == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).message);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).from)
                .child(userMessagesList.get(position).to)
                .child(userMessagesList.get(position).messageID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //changed here to undo and remove interface here in constructor and from chat activity
                    //also put displaychat content from on create to in a method
                    refresher.refreshAdapter();
                    Toast.makeText(holder.itemView.getContext(), "Deleted!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Could not delete!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceivedMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).to)
                .child(userMessagesList.get(position).from)
                .child(userMessagesList.get(position).messageID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Could not delete!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForAll(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).to)
                .child(userMessagesList.get(position).from)
                .child(userMessagesList.get(position).messageID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootRef.child("Messages").child(userMessagesList.get(position).from)
                            .child(userMessagesList.get(position).to)
                            .child(userMessagesList.get(position).messageID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(holder.itemView.getContext(), "Could not delete!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessageText, receiverMessageText;
        CircleImageView receiverProfileImage;
        ImageView messageSenderImageView, messageReceiverImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_msg_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_msg_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderImageView = itemView.findViewById(R.id.messageSenderImageView);
            messageReceiverImageView = itemView.findViewById(R.id.messageReceiverImageView);
        }
    }
    public interface Refresher{
        void refreshAdapter();
    }
}
