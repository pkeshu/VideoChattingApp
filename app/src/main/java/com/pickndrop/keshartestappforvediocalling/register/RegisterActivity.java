package com.pickndrop.keshartestappforvediocalling.register;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.main.MainActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    @BindView(R.id.ccp)
    protected CountryCodePicker ccp;
    @BindView(R.id.phoneText)
    protected EditText phoneNoEdt;
    @BindView(R.id.codeText)
    protected EditText codeEdt;
    @BindView(R.id.continueNextButton)
    protected Button continueBtn;
    @BindView(R.id.phoneAuth)
    protected RelativeLayout phoneAuth;

    private String checker = "", phoneNumber = "";
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        ccp.registerCarrierNumberEditText(phoneNoEdt);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d(TAG, "onVerificationFailed: "+e.getMessage());
                Toast.makeText(RegisterActivity.this, "Invalid Phone Number!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                phoneAuth.setVisibility(View.VISIBLE);
                continueBtn.setText("Continue");
                codeEdt.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendToken = forceResendingToken;

                phoneAuth.setVisibility(View.GONE);
                checker = "Code Sent";
                continueBtn.setText("Submit");
                codeEdt.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(RegisterActivity.this, "Code has been sent, please check", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @OnClick(R.id.continueNextButton)
    protected void continueBtnPressed(View view) {
        if (continueBtn.getText().toString().equals("Submit") || checker.equals("Code Sent")) {

            String verificationCode = codeEdt.getText().toString();
            if (verificationCode.equals("")) {
                Toast.makeText(this, "Please write verification code", Toast.LENGTH_SHORT).show();
            } else {
                loadingBar.setTitle("Code Verification");
                loadingBar.setMessage("Please wait, while we are verifying your code");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }

        } else {
            phoneNumber = ccp.getFullNumberWithPlus();
            Log.d(TAG, "continueBtnPressed: " + phoneNumber);
            if (!phoneNoEdt.getText().toString().equals("")) {
                loadingBar.setTitle("Phone Number Verification");
                loadingBar.setMessage("Please wait, while we are verifying your phone number.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthProvider.getInstance()
                        .verifyPhoneNumber(
                                phoneNumber,
                                60,
                                TimeUnit.SECONDS,
                                this,
                                callbacks
                        );

            } else {
                Toast.makeText(this, "Please Write Valid number!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Success");
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, "Congratulation you logged in successfully", Toast.LENGTH_SHORT).show();
//                            sendUserToMainActivity();
                            checkUserInDatabase();
                        } else {
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(RegisterActivity.this, "Invalid Credential", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    private void checkUserInDatabase() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
        userRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            sendUserToMainActivity();
                        } else {
                            sendUserToSaveActivity();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });
    }

    private void sendUserToSaveActivity() {
        startActivity(new Intent(this, SaveDataActivity.class));
        finish();
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            checkUserInDatabase();
            finish();
        }
    }
}
