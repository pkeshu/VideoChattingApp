package com.pickndrop.keshartestappforvediocalling.main.ui.find_people;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.models.Contacts;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class FindPeopleActivity extends AppCompatActivity implements FindPeopleAdapter.FindPeopleViewHolder.OnItemClick {
    private static final String TAG = "FindPeopleActivity";
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.search_user_text)
    protected EditText searchEdt;

    @BindView(R.id.find_people_recycler_view)
    protected RecyclerView recyclerView;
    private String str = "";

    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);
        ButterKnife.bind(this);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @OnTextChanged(R.id.search_user_text)
    protected void onTextChange(CharSequence input) {
        if (searchEdt.getText().toString().equals("")) {
            Toast.makeText(this, "Please write to search name!", Toast.LENGTH_SHORT).show();
        } else {
            str = input.toString();
            onStart();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options;
        if (str.equals("")) {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef, Contacts.class)
                    .build();
        } else {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef.orderByChild("name").startAt(str)
                            .endAt(str + "\uf8ff"), Contacts.class)
                    .build();
        }

        FirebaseRecyclerAdapter<Contacts, FindPeopleAdapter.FindPeopleViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, FindPeopleAdapter.FindPeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindPeopleAdapter.FindPeopleViewHolder holder, int i, @NonNull Contacts contacts) {
                holder.setData(contacts);
                holder.setItemClickListerner(FindPeopleActivity.this::ItemClickListerner);
            }

            @NonNull
            @Override
            public FindPeopleAdapter.FindPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FindPeopleAdapter.FindPeopleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.people_item_layout, parent, false));
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void ItemClickListerner(View view, Contacts contacts, int position) {
        Log.d(TAG, "ItemClickListerner: " + contacts.getName());
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("contactData", contacts);
        startActivity(intent);
    }
}
