package com.hriportfolio.speaketh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hriportfolio.speaketh.Utilities.Utils;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {


    @BindView(R.id.phone_number_input)
    EditText phone_number_input;
    @BindView(R.id.verification_code)
    EditText verification_code;
    @BindView(R.id.get_verification_code)
    Button get_verification_code_button;
    @BindView(R.id.submit_verification_code)
    Button submit_verification_code_button;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                pd.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Provide a valid number",
                        Toast.LENGTH_SHORT).show();
                phone_number_input.setVisibility(View.VISIBLE);
                get_verification_code_button.setVisibility(View.VISIBLE);

                verification_code.setVisibility(View.GONE);
                submit_verification_code_button.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                pd.dismiss();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent!",
                        Toast.LENGTH_SHORT).show();

                phone_number_input.setVisibility(View.GONE);
                get_verification_code_button.setVisibility(View.GONE);

                verification_code.setVisibility(View.VISIBLE);
                submit_verification_code_button.setVisibility(View.VISIBLE);
            }

        };
    }

    @OnClick(R.id.get_verification_code)
    void getVerificationCode() {

        String phoneNumber = phone_number_input.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(PhoneLoginActivity.this, "Provide a valid number",
                    Toast.LENGTH_SHORT).show();
        } else {
            pd = Utils.createProgressDialog(this);
            pd.show();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    PhoneLoginActivity.this,               // Activity (for callback binding)
                    callbacks);        // OnVerificationStateChangedCallbacks
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            sendUserToMainActivity();
                        } else {
                            pd.dismiss();
                            String msg = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error : "+msg,Toast
                                    .LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent i = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(i);
        finish();
    }

    @OnClick(R.id.submit_verification_code)
    void submitVerificationCode() {
        phone_number_input.setVisibility(View.INVISIBLE);
        get_verification_code_button.setVisibility(View.INVISIBLE);

        String code = verification_code.getText().toString();
        if(!TextUtils.isEmpty(code)){
            pd.show();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
    }
}
