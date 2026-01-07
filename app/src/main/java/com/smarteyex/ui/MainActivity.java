package com.smarteyex.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MenuItem;
import com.smarteyex.service.SmartEyeXService;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            requestPermissions();
            startService(new Intent(this, SmartEyeXService.class));
            setupNavigation();
            loadFragment(new HomeFragment());
        } catch (Exception e) {
            e.printStackTrace();  // Error handling
        }
    }

    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS
        };
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    private void setupNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        try {
            if (item.getItemId() == R.id.nav_home) fragment = new HomeFragment();
            else if (item.getItemId() == R.id.nav_status) fragment = new StatusFragment();
            else if (item.getItemId() == R.id.nav_log) fragment = new LogFragment();
            else if (item.getItemId() == R.id.nav_audio) fragment = new AudioFragment();
            else if (item.getItemId() == R.id.nav_camera) fragment = new CameraFragment();
            else if (item.getItemId() == R.id.nav_memory) fragment = new MemoryFragment();
            else if (item.getItemId() == R.id.nav_permissions) fragment = new PermissionsFragment();
            else if (item.getItemId() == R.id.nav_hardware) fragment = new HardwareFragment();
            else if (item.getItemId() == R.id.nav_settings) fragment = new SettingsFragment();
            if (fragment != null) loadFragment(fragment);
            drawerLayout.closeDrawers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
