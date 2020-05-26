package com.pickndrop.keshartestappforvediocalling.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.pickndrop.keshartestappforvediocalling.R;
import com.pickndrop.keshartestappforvediocalling.main.ui.SettingsFragment;
import com.pickndrop.keshartestappforvediocalling.register.RegisterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.nav_view)
    protected BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initBottomNavBar();
    }

    private void initBottomNavBar() {


//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_settings, R.id.navigation_notifications)
//                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        navView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home: {
                popUp(R.id.navigation_home);
                break;
            }
            case R.id.navigation_settings: {
                popUp(R.id.navigation_settings);
                break;
            }
            case R.id.navigation_notifications: {
                popUp(R.id.navigation_notifications);
                break;
            }
            case R.id.logout: {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                finish();
                break;
            }
        }
        return true;
    }

    private void popUp(int screenId) {
        NavOptions navOptions = new NavOptions
                .Builder()
                .setPopUpTo(R.id.mobile_navigation, true)
                .build();

        Navigation.findNavController(this, R.id.nav_host_fragment)
                .navigate(screenId,
                        null,
                        navOptions);
    }

}
