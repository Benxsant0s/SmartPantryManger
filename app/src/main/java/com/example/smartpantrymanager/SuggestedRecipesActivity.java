package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows only the recipes the user can make right now, using the
 * strict-matching rule implemented in RecipeMatcher.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmpty, textCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle("Suggested Recipes");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerRecipes);
        textEmpty = findViewById(R.id.textEmpty);
        textCount = findViewById(R.id.textCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            // Intent passes the recipe id to the detail screen
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    // Re-run the matching every time the screen is shown
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> allRecipes = db.getAllRecipes();
        List<Recipe> suggested = RecipeMatcher.getSuggested(allRecipes, pantry);

        adapter.setRecipes(suggested);

        if (suggested.isEmpty()) {
            // Empty state: never show a blank screen
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            textCount.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            textCount.setVisibility(View.VISIBLE);
            textCount.setText("You can make " + suggested.size()
                    + (suggested.size() == 1 ? " recipe" : " recipes") + " right now");
        }
    }

    // Back arrow in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}