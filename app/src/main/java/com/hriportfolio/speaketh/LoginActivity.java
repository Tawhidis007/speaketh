package com.hriportfolio.speaketh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hriportfolio.speaketh.Utilities.KeyString;
import com.hriportfolio.speaketh.Utilities.SharedPreferenceManager;
import com.hriportfolio.speaketh.Utilities.Utils;

public class LoginActivity extends AppCompatActivity {

    //views
    @BindView(R.id.login_email)
    EditText login_email;
    @BindView(R.id.login_password)
    EditText login_password;
//    @BindView(R.id.forgot_password)
//    TextView forgot_password;

    ProgressDialog pd;

    SharedPreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        initPref();
        mAuth = FirebaseAuth.getInstance();

//        forgot_password.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

    private void initPref(){
        preferenceManager = new SharedPreferenceManager(this, KeyString.PREF_NAME);
        if(preferenceManager.getValue(KeyString.SIGN_IN_FLAG,false)){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.login_button)
    void login() {
        String email = login_email.getText().toString();
        String password = login_password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Please enter email!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Please enter password!", Toast.LENGTH_SHORT).show();
        } else {
            pd = Utils.createProgressDialog(this);
            pd.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String currentUserId = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    usersRef.child(currentUserId).child("device_token").setValue(deviceToken)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                 if(task.isSuccessful()){
                                     pd.dismiss();
                                     preferenceManager.setValue(KeyString.SIGN_IN_FLAG, true);
                                     sendUserToMainActivity();
                                     Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                 }
                                }
                            });

                } else {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Error : " + task.getException().toString()
                            , Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @OnClick(R.id.register_button_in_login_page)
    void registerFromLogin() {
        sendUserToRegisterActivity();
    }

    @OnClick(R.id.phone_login_button)
    void loginWithPhone() {
        Intent i = new Intent(LoginActivity.this,PhoneLoginActivity.class);
        startActivity(i);
    }

    private void sendUserToMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void sendUserToRegisterActivity() {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }

}
