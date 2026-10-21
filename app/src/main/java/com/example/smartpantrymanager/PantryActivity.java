package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen: shows every ingredient in the pantry from the database.
 * The toolbar menu lets the user open the Suggested Recipes screen.
 */
public class PantryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerPantry);
        textEmpty = findViewById(R.id.textEmpty);
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

    // Reload every time we come back, so edits/adds show immediately
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        adapter.setItems(items);
        boolean empty = items.isEmpty();
        textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
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

    // Creates the toolbar menu (res/menu/menu_pantry.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pantry, menu);
        return true;
    }

    // Handles taps on the toolbar menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_suggestions) {
            startActivity(new Intent(this, SuggestedRecipesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}