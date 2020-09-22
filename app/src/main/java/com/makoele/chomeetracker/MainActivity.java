package com.makoele.chomeetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.makoele.chomeetracker.Fragments.AddFriendsFragment;
import com.makoele.chomeetracker.Fragments.ChatFragment;
import com.makoele.chomeetracker.Fragments.GroupFragment;
import com.makoele.chomeetracker.Fragments.HomeFragment;
import com.makoele.chomeetracker.Fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //BottomNav 1 -- getting bottom navigation view and attaching the listener
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //Sets HomeFragment as default
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

    }

    //BottomNav 2 -- Navigation View on Select
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                        case R.id.nav_add_friend:
                            selectedFragment = new AddFriendsFragment();
                            break;
                        case R.id.nav_chat:
                            selectedFragment = new ChatFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                        case R.id.nav_groups:
                            selectedFragment = new GroupFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };




}

