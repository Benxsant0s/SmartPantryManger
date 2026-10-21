package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanger.database.DatabaseHelper;
import com.example.smartpantrymanger.model.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen: shows every ingredient in the pantry from the database.
 */
public class PantryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerPantry);
        emptyState = findViewById(R.id.emptyState);
        bottomNav = findViewById(R.id.bottomNav);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new ArrayList<>(), new PantryAdapter.OnItemActionListener() {
            @Override
            public void onEdit(PantryItem item) {
                // Intent passes the item id to the Add/Edit screen
                Intent intent = new Intent(PantryActivity.this, AddEditIngredientActivity.class);
                intent.putExtra("item_id", item.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(PantryItem item) {
                confirmDelete(item);
            }
        });
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v ->
                startActivity(new Intent(PantryActivity.this, AddEditIngredientActivity.class)));
    }

    // Reload every time we come back, so edits/adds/settings show immediately
    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.setup(this, bottomNav, R.id.nav_pantry);

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE);
        adapter.setHighlightExpiring(prefs.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true));
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        adapter.setItems(items);
        boolean empty = items.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteItem(item.getId());
                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                    loadItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}