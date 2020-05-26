package com.pickndrop.keshartestappforvediocalling.main.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.main.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    private Context context;

    @BindView(R.id.settings_profile_image)
    protected ImageView profileImage;

    @BindView(R.id.username_settings)
    protected EditText usernameEdt;

    @BindView(R.id.bio_settings)
    protected EditText bioEdt;

    @BindView(R.id.save_settings)
    protected Button saveBtn;

    public static final int PICK_IMAGE = 1;
    private Uri imageUri;

    private StorageReference userStorageReference;

    private String downloadUrl;

    private DatabaseReference userRef;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = inflater.getContext();
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        userStorageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        progressDialog = new ProgressDialog(context);
        retriveUserInfo();
    }

    @OnClick(R.id.save_settings)
    protected void saveBtnPressed(View view) {
        Log.d(TAG, "saveBtnPressed: is clicked");
        saveUserData();
    }

    private void saveUserData() {
        final String username = usernameEdt.getText().toString();
        final String userStatus = bioEdt.getText().toString();
        progressDialog.setTitle("Account Settings");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        Log.d(TAG, "saveUserData: save ");
        if (imageUri == null) {
            Log.d(TAG, "saveUserData: image null");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")) {
                        saveInfoOnlyWithoutImage(username, userStatus);
                    } else {
                        Log.d(TAG, "onDataChange: select image first");
                        progressDialog.dismiss();
                        Toast.makeText(context, "Please select Image First!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    progressDialog.dismiss();
                }
            });
        } else if (username.equals("")) {
            Log.d(TAG, "saveUserData: usename null");
            Toast.makeText(context, "Username should not be empty!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        } else if (userStatus.equals("")) {
            Log.d(TAG, "saveUserData: userstatus null");
            Toast.makeText(context, "User status should not be empty!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        } else {
            Log.d(TAG, "saveUserData: all set");
            final StorageReference filePath = userStorageReference.child(FirebaseAuth
                    .getInstance()
                    .getCurrentUser()
                    .getUid());
            final UploadTask uploadTask = filePath.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    downloadUrl = filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadUrl = task.getResult().toString();
                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name", username);
                        profileMap.put("status", userStatus);
                        profileMap.put("image", downloadUrl);
                        profileMap.put("mobileNumber", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(context, MainActivity.class));
                                            ((Activity) context).finish();
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "Profile Settings has been updated!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            });
        }
    }

    private void saveInfoOnlyWithoutImage(String username, String userStatus) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Account Settings");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        if (!username.equals("") && !userStatus.equals("")) {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name", username);
            profileMap.put("status", userStatus);
            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(context, MainActivity.class));
                                ((Activity) context).finish();
                                progressDialog.dismiss();
                                Toast.makeText(context, "Profile Settings has been updated!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(context, "Username and user status should not be empty!", Toast.LENGTH_SHORT).show();
        }


    }

    @OnClick(R.id.settings_profile_image)
    protected void imageClicked(View view) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    2000);
        } else {
            startGallery();
        }
    }

    private void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_GET_CONTENT);
        cameraIntent.setType("image/*");
        startActivityForResult(cameraIntent, PICK_IMAGE);
    }

    private void retriveUserInfo() {
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String imageDb = dataSnapshot.child("image").getValue().toString();
                            String nameDb = dataSnapshot.child("name").getValue().toString();
                            String statusDb = dataSnapshot.child("status").getValue().toString();

                            usernameEdt.setText(nameDb);
                            bioEdt.setText(statusDb);
                            Picasso.get().load(imageDb).placeholder(context.getResources().getDrawable(R.drawable.profile_image)).into(profileImage);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            Log.d(TAG, "onActivityResult: " + imageUri.toString());
            profileImage.setImageURI(imageUri);
        }
    }
}
