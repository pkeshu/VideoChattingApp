package com.pickndrop.keshartestappforvediocalling.main.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class NotificationFragment extends Fragment {
    private static final String TAG = "NotificationFragment";
    private Context context;

    @BindView(R.id.notification_recycler)
    protected RecyclerView notificationRecyclerVIew;

    private DatabaseReference friendRequestRef, contactsRef, usersRef;
    private String currentUserId = "";

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = inflater.getContext();
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initNotificationRecycler();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
    }

    private void initNotificationRecycler() {
        notificationRecyclerVIew.setHasFixedSize(true);
        notificationRecyclerVIew.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(friendRequestRef.child(currentUserId), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, NotificationAdapter.ContactViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, NotificationAdapter.ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotificationAdapter.ContactViewHolder holder, int i, @NonNull Contacts contacts) {
                holder.accept_btn.setVisibility(View.VISIBLE);
                holder.declinedBtn.setVisibility(View.VISIBLE);
//                holder.setData(contacts);
                final String listUserId = getRef(i).getKey();
                DatabaseReference requestTypeRef = getRef(i).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("received")) {
                                holder.itemView.setVisibility(View.VISIBLE);
                                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            final String imageStr = dataSnapshot.child("image").getValue().toString();
                                            final String nameStr = dataSnapshot.child("name").getValue().toString();
                                            Picasso.get().load(imageStr).into(holder.userImage);
                                            holder.nameTxtV.setText(nameStr);
                                        }
                                        final String nameStr = dataSnapshot.child("name").getValue().toString();
                                        holder.nameTxtV.setText(nameStr);

                                        holder.accept_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                acceptFriendRequest(listUserId);
                                            }
                                        });

                                        holder.declinedBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                cancelFriendRequest(listUserId);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                                    }
                                });
                            } else {
                                holder.itemView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });
            }

            @NonNull
            @Override
            public NotificationAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new NotificationAdapter.ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.find_people_item_layout, parent, false));
            }
        };
        notificationRecyclerVIew.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void acceptFriendRequest(String listUserId) {
        contactsRef.child(currentUserId)
                .child(listUserId)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(listUserId)
                                    .child(currentUserId)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                friendRequestRef.child(currentUserId)
                                                        .child(listUserId)
                                                        .removeValue().
                                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    friendRequestRef.child(listUserId)
                                                                            .child(currentUserId)
                                                                            .removeValue().
                                                                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(context, "New Contact Saved.", Toast.LENGTH_SHORT).show();
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

    private void cancelFriendRequest(String listUserId) {
        friendRequestRef.child(currentUserId)
                .child(listUserId)
                .removeValue().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(listUserId)
                                    .child(currentUserId)
                                    .removeValue().
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, "Friend Request Cancelled.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
