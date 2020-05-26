package com.pickndrop.keshartestappforvediocalling.vedio_chat;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.register.RegisterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VedioChatActivity extends AppCompatActivity
        implements Session.SessionListener,
        Publisher.PublisherListener {
    private static final String TAG = "VedioChatActivity";
    public static final String API_KEY = "46762312";
    public static final String SESSION_ID = "1_MX40Njc2MjMxMn5-MTU5MDQ4NDQ3NTIyNn5sWlc5aFlTc2JPdTFtTWx0UUNJQld4ZjB-fg";
    public static final String TOKEN = "T1==cGFydG5lcl9pZD00Njc2MjMxMiZzaWc9YTYwNmJkNjBhMjlmNTRjZTM1ODRmNjEyOWYwZjkzMTg0MTlhNWE3NzpzZXNzaW9uX2lkPTFfTVg0ME5qYzJNak14TW41LU1UVTVNRFE0TkRRM05USXlObjVzV2xjNWFGbFRjMkpQZFRGdFRXeDBVVU5KUWxkNFpqQi1mZyZjcmVhdGVfdGltZT0xNTkwNDg0NTUwJm5vbmNlPTAuMDQzMjA4OTQyMjgzNjgyMzMmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5MzA3NjU0NiZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    public static final int RC_VEDIO_APP_PERMISSION = 124;

    private DatabaseReference userRef;
    private String userId = "";

    private Session mSession;
    private Publisher publisher;
    private Subscriber subscriber;

    @BindView(R.id.close_vedio_chat)
    protected ImageView closeVedioChatBtn;
    @BindView(R.id.publisher_container)
    protected FrameLayout publisherContainer;
    @BindView(R.id.subscriber_container)
    protected FrameLayout subscribeContainerr;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedio_chat);
        ButterKnife.bind(this);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        requestPermission();

    }

    @OnClick(R.id.close_vedio_chat)
    protected void ClosedVedioPressed(View view) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userId).hasChild("Ringing")) {
                    userRef.child(userId).child("Ringing").removeValue();

                    if (publisher != null) {
                        publisher.destroy();
                    }
                    if (subscriber != null) {
                        subscriber.destroy();
                    }

                    startActivity(new Intent(VedioChatActivity.this, RegisterActivity.class));
                    finish();
                }
                if (dataSnapshot.child(userId).hasChild("Calling")) {
                    userRef.child(userId).child("Calling").removeValue();
                    if (publisher != null) {
                        publisher.destroy();
                    }
                    if (subscriber != null) {
                        subscriber.destroy();
                    }
                    startActivity(new Intent(VedioChatActivity.this, RegisterActivity.class));
                    finish();
                } else {
                    if (publisher != null) {
                        publisher.destroy();
                    }
                    if (subscriber != null) {
                        subscriber.destroy();
                    }
                    startActivity(new Intent(VedioChatActivity.this, RegisterActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @AfterPermissionGranted(RC_VEDIO_APP_PERMISSION)
    private void requestPermission() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VedioChatActivity.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "Hey,This app needs the Mic and Camera permission. Please Allow", RC_VEDIO_APP_PERMISSION, perms);
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.i(TAG, "onConnected: Session Connected.");
        publisher = new Publisher.Builder(this).build();
        publisher.setPublisherListener(VedioChatActivity.this);

        publisherContainer.addView(publisher.getView());

        if (publisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
        }
        session.publish(publisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "onDisconnected: Stream Disconnected.");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TAG, "onStreamReceived: Stream Received.");
        if (subscriber == null) {
            subscriber = new Subscriber.Builder(this, stream).build();
            session.subscribe(subscriber);
            subscribeContainerr.addView(subscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TAG, "onStreamDropped: Stream Dropped");
        if (subscriber != null) {
            subscriber = null;
            subscribeContainerr.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(TAG, "onError: Stream Error");
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }
}
