package com.hichamzouagui.energymanager.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.hichamzouagui.energymanager.R;

/** Écran unique avec une barre de navigation : tableau de bord, appareils, relevés, réglages. */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_TAB = "selected_tab";

    private int selectedTabId = R.id.tab_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt(STATE_SELECTED_TAB, R.id.tab_dashboard);
        }

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        // On restaure l'onglet avant de brancher l'écouteur, pour ne pas déclencher
        // une transaction inutile : le fragment est soit recréé, soit restauré ci-dessous.
        navigation.setSelectedItemId(selectedTabId);
        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectedTabId = item.getItemId();
                showFragment(selectedTabId);
                return true;
            }
        });

        if (savedInstanceState == null) {
            showFragment(selectedTabId);
        } else {
            // Le fragment est restauré par le FragmentManager : seul le titre est à remettre.
            setTitle(titleOf(selectedTabId));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, selectedTabId);
    }

    private void showFragment(int tabId) {
        Fragment fragment;
        if (tabId == R.id.tab_devices) {
            fragment = new DevicesFragment();
        } else if (tabId == R.id.tab_readings) {
            fragment = new ReadingsFragment();
        } else if (tabId == R.id.tab_settings) {
            fragment = new SettingsFragment();
        } else {
            fragment = new DashboardFragment();
        }

        setTitle(titleOf(tabId));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private int titleOf(int tabId) {
        if (tabId == R.id.tab_devices) {
            return R.string.tab_devices;
        }
        if (tabId == R.id.tab_readings) {
            return R.string.tab_readings;
        }
        if (tabId == R.id.tab_settings) {
            return R.string.tab_settings;
        }
        return R.string.app_name;
    }
}
