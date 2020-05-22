package com.hriportfolio.speaketh;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hriportfolio.speaketh.Utilities.Utils;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.register_email)
    EditText register_email;
    @BindView(R.id.register_password)
    EditText register_password;

    private String email;
    private String password;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);


        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @OnClick(R.id.register_user_button)
    void register_user(){
        createNewAcc();
    }

    private void createNewAcc() {
        email = register_email.getText().toString();
        password = register_password.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this,"Please enter email!",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this,"Please enter password!",Toast.LENGTH_SHORT).show();
        }
        else{
            pd = Utils.createProgressDialog(this);
            pd.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                String currentUserId = mAuth.getCurrentUser().getUid();
                rootRef.child("Users").child(currentUserId).setValue("");

                rootRef.child("Users").child(currentUserId).child("device_token").setValue(deviceToken);

                Toast.makeText(RegisterActivity.this,"Account created successfully!",Toast.LENGTH_SHORT).show();
                sendUserToLoginActivity();
                pd.dismiss();
            }else{
                String message = task.getException().toString();
                pd.dismiss();
                Toast.makeText(RegisterActivity.this,"Error : "+message,Toast.LENGTH_SHORT).show();
            }
            });
        }
    }

    @OnClick(R.id.already_have_account_button)
    void alreadyHasAcc(){
        sendUserToLoginActivity();
    }

    private void sendUserToLoginActivity() {
        Intent i = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(i);
    }
}
