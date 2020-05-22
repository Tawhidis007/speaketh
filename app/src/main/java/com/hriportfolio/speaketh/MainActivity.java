package com.hriportfolio.speaketh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import butterknife.ButterKnife;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hriportfolio.speaketh.Utilities.KeyString;
import com.hriportfolio.speaketh.Utilities.SharedPreferenceManager;
import com.hriportfolio.speaketh.Utilities.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    SharedPreferenceManager preferenceManager;
    private DatabaseReference rootRef;

    //accountInfo
    private String user_name;
    private String user_status;
    private String user_pro_pic;

    private ProgressDialog pd;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initPref();
        verifyUserExistence();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Speaketh");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);


        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
            updateUserStatus("online");
    }


    private void verifyUserExistence() {
        pd = Utils.createProgressDialog(this);
        pd.show();
        String currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()) {

                    String uName = dataSnapshot.child("name").getValue().toString();
                    String uStatus = dataSnapshot.child("status").getValue().toString();
                    String uid = dataSnapshot.child("uid").getValue().toString();
                    if(dataSnapshot.hasChild("image")){
                        String uImage = dataSnapshot.child("image").getValue().toString();
                        preferenceManager.setValue(KeyString.PROFILE_PICTURE_URL,uImage);
                    }
                    Log.d("test_data exists",uName);
                    preferenceManager.setValue(KeyString.USER_STATUS, uStatus);
                    preferenceManager.setValue(KeyString.USER_NAME, uName);
                    preferenceManager.setValue(KeyString.UID, uid);
                    pd.dismiss();
                } else {
                    Log.d("test_doesnot exist",preferenceManager.getValue(KeyString.USER_NAME,""));
                    pd.dismiss();
                    sendUserToSettingActiviy();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initPref() {
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        user_name = preferenceManager.getValue(KeyString.USER_NAME, "");
        user_status = preferenceManager.getValue(KeyString.USER_STATUS, "");
        user_pro_pic = preferenceManager.getValue(KeyString.PROFILE_PICTURE_URL, "");
        Log.d("test_initpref ",user_name);

        if (user_name.equals("")) {
            verifyUserExistence();
        } else {
            //set up views from sharedpref
        }
    }

    private void sendUserToLoginActivity() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.menu_find_people) {
            Intent i = new Intent(MainActivity.this,FindFriends.class);
            startActivity(i);
        }
        if (item.getItemId() == R.id.menu_settings) {
            sendUserToSettingActiviy();
        }
        if (item.getItemId() == R.id.logout) {
            updateUserStatus("offline");
            preferenceManager.clear();
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.menu_create_group) {
            RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.create_dialog_layout);
        dialog.setTitle("Create a new group");
        Button ok = dialog.findViewById(R.id.create_group_button);
        Button cancel = dialog.findViewById(R.id.cancel_group_button);
        EditText et = dialog.findViewById(R.id.create_group_et);

        ok.setOnClickListener(view -> {
            String groupName = et.getText().toString();
            if (groupName.equals("")) {
                Toast.makeText(MainActivity.this, "Please provide group name!"
                        , Toast.LENGTH_SHORT).show();
            }else{
                createNewGroup(groupName);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void createNewGroup(String groupName) {
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+" is created successfully!",
                            Toast.LENGTH_SHORT).show();
                }
                });
    }

    void sendUserToSettingActiviy() {
        Intent i = new Intent(MainActivity.this, SettingActivity.class);
        //to stop user from coming back to this activity
        startActivity(i);
    }

    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);


    }

    @Override
    public void onBackPressed() {
    }
}
