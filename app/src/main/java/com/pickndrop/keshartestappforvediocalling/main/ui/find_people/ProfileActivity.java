package com.pickndrop.keshartestappforvediocalling.main.ui.find_people;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.models.Contacts;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private Contacts contacts;

    @BindView(R.id.user_image)
    protected ImageView userImage;

    @BindView(R.id.username)
    protected TextView nameTxtV;

    @BindView(R.id.add_friend_btn)
    protected Button acceptBtn;

    @BindView(R.id.cancel_friend_btn)
    protected Button cancleBtn;

    private String senderUserId = "";
    private String receiverUserId = "";

    private FirebaseAuth mAuth;
    private String currentState = "new";

    private DatabaseReference friendRequestRef, contactsRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        getIntentValue();
        setDataInView();

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        receiverUserId = contacts.getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        manageClickEvents();
    }

    private void manageClickEvents() {

        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)) {
                            String requestType = dataSnapshot.child(receiverUserId)
                                    .child("request_type")
                                    .getValue().toString();
                            if (requestType.equals("sent")) {
                                currentState = "request_sent";
                                acceptBtn.setText("Cancel Friend Request");
                            }
                            if (requestType.equals("received")) {
                                currentState = "request_received";
                                acceptBtn.setText("Accept Friend Request");
                                cancleBtn.setVisibility(View.VISIBLE);
                            }

                        } else {
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserId)) {
                                                currentState = "friends";
                                                acceptBtn.setText("Delete Contact");
                                                cancleBtn.setVisibility(View.GONE);
                                            } else {
                                                currentState = "new";
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });

        if (contacts.getUid().equals(senderUserId)) {
            acceptBtn.setVisibility(View.GONE);
            cancleBtn.setVisibility(View.GONE);
        } else {

        }
    }

    private void setDataInView() {
        Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(userImage);
        nameTxtV.setText(contacts.getName());
    }

    private void getIntentValue() {
        contacts = getIntent().getExtras().getParcelable("contactData");
    }

    @OnClick(R.id.add_friend_btn)
    protected void addfreidnRequestClicked(View view) {
        if (currentState.equals("new")) {
            sendFriendRequest();
        }
        if (currentState.equals("request_sent")) {
            cancelFriendRequest();
        }
        if (currentState.equals("request_received")) {
            acceptFriendRequest();
        }
        if (currentState.equals("friends")) {
            deleteFriend();
        }

    }

    private void deleteFriend() {
        contactsRef.child(senderUserId)
                .child(receiverUserId)
                .removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId)
                                    .child(senderUserId)
                                    .removeValue().
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                currentState = "new";
                                                acceptBtn.setText("Add Friend");
                                                cancleBtn.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        contactsRef.child(receiverUserId)
                .child(senderUserId)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(senderUserId)
                                    .child(receiverUserId)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                friendRequestRef.child(senderUserId)
                                                        .child(receiverUserId)
                                                        .removeValue().
                                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    friendRequestRef.child(receiverUserId)
                                                                            .child(senderUserId)
                                                                            .removeValue().
                                                                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        currentState = "friends";
                                                                                        acceptBtn.setText("Delete Contact");
                                                                                        cancleBtn.setVisibility(View.GONE);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                ;
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId)
                .child(receiverUserId)
                .removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiverUserId)
                                    .child(senderUserId)
                                    .removeValue().
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                currentState = "new";
                                                acceptBtn.setText("Add Friend");
                                                cancleBtn.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendRequestRef.child(receiverUserId).child(senderUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        currentState = "request_sent";
                                        acceptBtn.setText("Cancel Friend Request");
                                        Toast.makeText(ProfileActivity.this, "Friend Request sent.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @OnClick(R.id.cancel_friend_btn)
    protected void cancelfreidnRequestClicked(View view) {
        cancelFriendRequest();
    }
}
