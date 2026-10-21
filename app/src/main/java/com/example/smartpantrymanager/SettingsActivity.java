package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;


public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Settings");

        bottomNav = findViewById(R.id.bottomNav);
        SwitchMaterial switchExpiry = findViewById(R.id.switchExpiry);
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Load the saved value (default: on)
        switchExpiry.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));

        // Save whenever the user changes it
        switchExpiry.setOnCheckedChangeListener((button, isChecked) -> {
            prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply();
            Toast.makeText(this, isChecked ? "Expiring-soon alerts on"
                    : "Expiring-soon alerts off", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.setup(this, bottomNav, R.id.nav_settings);
    }
}