package com.hriportfolio.speaketh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hriportfolio.speaketh.Utilities.KeyString;
import com.hriportfolio.speaketh.Utilities.SharedPreferenceManager;
import com.hriportfolio.speaketh.Utilities.Utils;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.profile_image)
    CircleImageView profile_image;
    @BindView(R.id.set_user_name)
    EditText user_name;
    @BindView(R.id.set_profile_status)
    EditText profile_status;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    SharedPreferenceManager preferenceManager;
    private StorageReference userProfileImagesRef;

    private ProgressDialog pd;
    private StorageTask uploadTask;
    private String proPicUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);

        proPicUrl = preferenceManager.getValue(KeyString.PROFILE_PICTURE_URL,"");
        retrieveUserInfo();
    }

    @OnClick(R.id.profile_image)
    void selectProfilePicture() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                pd = Utils.createProgressDialog(this);
                pd.show();
                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImagesRef.child(currentUserId + ".jpg");
                uploadTask = filePath.putFile(resultUri);
                uploadTask.continueWithTask((Continuation) task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        proPicUrl = downloadUrl.toString();
                        Picasso.get().load(proPicUrl).into(profile_image);
                        preferenceManager.setValue(KeyString.PROFILE_PICTURE_URL,proPicUrl);
                        pd.dismiss();

//                        rootRef.child("Users").child(currentUserId).child("image").setValue(proPicUrl)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Toast.makeText(SettingActivity.this, "Success!",
//                                                    Toast.LENGTH_SHORT).show();
//                                            pd.dismiss();
//                                        } else {
//                                            pd.dismiss();
//                                            Toast.makeText(SettingActivity.this, "Failed!",
//                                                    Toast.LENGTH_SHORT).show();
//                                            task.getException().printStackTrace();
//                                        }
//                                    }
//                                });
                    } else {
                        Toast.makeText(SettingActivity.this, "Failed! Try again.",
                                Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
            }
        }
    }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))) {
                            String uName = dataSnapshot.child("name").getValue().toString();
                            String uStatus = dataSnapshot.child("status").getValue().toString();
                            String uImage = dataSnapshot.child("image").getValue().toString();

                            user_name.setText(uName);
                            profile_status.setText(uStatus);
                            Picasso.get().load(uImage).into(profile_image);
                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                            String uName = dataSnapshot.child("name").getValue().toString();
                            String uStatus = dataSnapshot.child("status").getValue().toString();
                            user_name.setText(uName);
                            profile_status.setText(uStatus);

                        } else {
                            Toast.makeText(SettingActivity.this, "Please setup profile!"
                                    , Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @OnClick(R.id.update_settings_button)
    void updateSettings() {
        String name = user_name.getText().toString();
        String status = profile_status.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(SettingActivity.this, "Please enter your name!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(SettingActivity.this, "Please enter your status!", Toast.LENGTH_SHORT).show();
        } else {
            String deviceToken = FirebaseInstanceId.getInstance().getToken();
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", name);
            profileMap.put("status", status);
            profileMap.put("device_token",deviceToken);
            if(!proPicUrl.equals("")){
                profileMap.put("image",proPicUrl);
            }

            rootRef.child("Users").child(currentUserId).setValue(profileMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //populate sharedPref
                    preferenceManager.setValue(KeyString.USER_STATUS, status);
                    preferenceManager.setValue(KeyString.USER_NAME, name);
                    preferenceManager.setValue(KeyString.UID, currentUserId);
                    Toast.makeText(SettingActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    sendUserBack();
                } else {
                    String msg = task.getException().toString();
                    Toast.makeText(SettingActivity.this, "Error : " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendUserBack() {
        Intent i = new Intent(SettingActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
