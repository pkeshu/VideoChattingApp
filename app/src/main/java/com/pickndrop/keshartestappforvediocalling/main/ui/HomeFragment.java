package com.pickndrop.keshartestappforvediocalling.main.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.calling.CallingActivity;
import com.pickndrop.keshartestappforvediocalling.main.ui.find_people.FindPeopleActivity;
import com.pickndrop.keshartestappforvediocalling.models.Contacts;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private Context context;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.find_people_btn)
    protected ImageView findPeopleBtn;

    @BindView(R.id.contact_list)
    protected RecyclerView contactListRecyclerView;
    private DatabaseReference friendRequestRef, contactsRef, usersRef;
    private String currentUserId = "";

    private FirebaseAuth mAuth;

    private String userName, profilePicture;

    private String calledBy = "";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initRecyclerView();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
    }

    private void initRecyclerView() {
        contactListRecyclerView.setHasFixedSize(true);
        contactListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @OnClick(R.id.find_people_btn)
    protected void findContactListBtn(View view) {
        startActivity(new Intent(context, FindPeopleActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();

        checkForReceivingCall();

        FirebaseRecyclerOptions options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactAdapter.ContactViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactAdapter.ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int i, @NonNull Contacts contacts) {
                final String listUserId = getRef(i).getKey();
                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            userName = dataSnapshot.child("name").getValue().toString();
                            profilePicture = dataSnapshot.child("image").getValue().toString();

                            holder.nameTxtV.setText(userName);
                            Picasso.get().load(profilePicture).into(holder.userImage);

                            holder.callBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(context, CallingActivity.class);
                                    intent.putExtra("visit_user_id", listUserId);
                                    startActivity(intent);
                                }
                            });

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
            public ContactAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ContactAdapter.ContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.people_item_layout, parent, false));
            }
        };

        contactListRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForReceivingCall() {

        usersRef.child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("ringing")) {
                            calledBy = dataSnapshot.child("ringing").getValue().toString();
                            Intent intent = new Intent(context, CallingActivity.class);
                            intent.putExtra("visit_user_id", calledBy);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });
//        usersRef.child(currentUserId)
//                .child("Calling")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.hasChild("calling")) {
//                            String calledFrom = dataSnapshot.child("calling").getValue().toString();
//                            Intent intent = new Intent(context, CallingActivity.class);
//                            intent.putExtra("visit_user_id", calledFrom);
//                            startActivity(intent);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
//                    }
//                });
    }
}