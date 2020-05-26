package com.pickndrop.keshartestappforvediocalling.calling;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.pickndrop.keshartestappforvediocalling.register.RegisterActivity;
import com.pickndrop.keshartestappforvediocalling.vedio_chat.VedioChatActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CallingActivity extends AppCompatActivity {
    private static final String TAG = "CallingActivity";

    @BindView(R.id.name_calling)
    protected TextView nameCalling;

    @BindView(R.id.profile_image_calling)
    protected ImageView profileImage;
    @BindView(R.id.make_call)
    protected ImageView acceptCallBtn;

    @BindView(R.id.cancel_call)
    protected ImageView cancelCallBtn;

    private String receiverUserId = "", receiverUserName = "", receiverUserImage = "";
    private String senderUserId = "", senderUserName = "", senderUserImage = "";
    private DatabaseReference userRef;
    private String checker = "";
    private String callingId = "", ringinId = "";
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        ButterKnife.bind(this);
        getIntentValue();
        mediaPlayer = MediaPlayer.create(this, R.raw.ring_for_call);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        getAndSetUserProfileInfo();


    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverUserId).exists()) {
                    receiverUserImage = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();
                    nameCalling.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }

                if (dataSnapshot.child(senderUserId).exists()) {
                    senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName = dataSnapshot.child(senderUserId).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void getIntentValue() {
        receiverUserId = getIntent().getStringExtra("visit_user_id");
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @OnClick(R.id.make_call)
    protected void makeCallBtnPressed(View view) {
        mediaPlayer.stop();
        final HashMap<String, Object> callingPickUpmap = new HashMap<>();
        callingPickUpmap.put("picked", "picked");
        userRef.child(senderUserId).child("Ringing")
                .updateChildren(callingPickUpmap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(CallingActivity.this, VedioChatActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    @OnClick(R.id.cancel_call)
    protected void cancelCallBtnPressed(View view) {
        mediaPlayer.stop();
        checker = "clicked";

        cancelCallingUser();
    }

    private void cancelCallingUser() {

        //from sender
        userRef.child(senderUserId).child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")) {
                            callingId = dataSnapshot.child("calling").getValue().toString();

                            userRef.child(callingId).child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                userRef.child(senderUserId)
                                                        .child("Calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    startActivity(new Intent(CallingActivity.this, RegisterActivity.class));
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                        } else {
                            startActivity(new Intent(CallingActivity.this, RegisterActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //from reciver side
        userRef.child(senderUserId).child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")) {
                            ringinId = dataSnapshot.child("ringing").getValue().toString();

                            userRef.child(ringinId).child("Calling")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                userRef.child(senderUserId)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    startActivity(new Intent(CallingActivity.this, RegisterActivity.class));
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });

                        } else {
                            startActivity(new Intent(CallingActivity.this, RegisterActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") &&
                        !dataSnapshot.hasChild("Calling") &&
                        !dataSnapshot.hasChild("Ringing")) {
                    HashMap<String, Object> callingInfo = new HashMap<>();
//                    callingInfo.put("uid", senderUserId);
//                    callingInfo.put("name", senderUserName);
//                    callingInfo.put("image", senderUserImage);
                    callingInfo.put("calling", receiverUserId);
                    userRef.child(senderUserId)
                            .child("Calling")
                            .updateChildren(callingInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        HashMap<String, Object> ringingInfo = new HashMap<>();
//                                        ringingInfo.put("uid", receiverUserId);
//                                        ringingInfo.put("name", receiverUserName);
//                                        ringingInfo.put("image", receiverUserImage);
                                        ringingInfo.put("ringing", senderUserId);
                                        userRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") &&
                        !dataSnapshot.child(senderUserId).hasChild("Calling")) {
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }
                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")) {
                    mediaPlayer.stop();
                    startActivity(new Intent(CallingActivity.this, VedioChatActivity.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
