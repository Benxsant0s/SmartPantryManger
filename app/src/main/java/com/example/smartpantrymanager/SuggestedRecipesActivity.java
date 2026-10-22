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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows recipes matched against the user's pantry.
 * Displays 100% ready-to-cook recipes first, and partially matched recipes with missing ingredients.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmpty, textCount;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Suggested Recipes");

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerRecipes);
        textEmpty = findViewById(R.id.textEmpty);
        textCount = findViewById(R.id.textCount);
        bottomNav = findViewById(R.id.bottomNav);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavHelper.setup(this, bottomNav, R.id.nav_recipes);
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> allRecipes = db.getAllRecipes();
        List<RecipeMatcher.RecipeMatch> matches = RecipeMatcher.getMatchResults(allRecipes, pantry);

        adapter.setMatches(matches);

        int readyCount = 0;
        for (RecipeMatcher.RecipeMatch match : matches) {
            if (match.isCanMake()) {
                readyCount++;
            }
        }
        int almostCount = matches.size() - readyCount;

        if (pantry.isEmpty()) {
            textEmpty.setText("Your pantry is empty. Tap Pantry below to add ingredients!");
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            textCount.setVisibility(View.GONE);
        } else if (matches.isEmpty()) {
            textEmpty.setText("No recipes match your ingredients yet. Try adding staples like salt, butter, or oil!");
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            textCount.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            textCount.setVisibility(View.VISIBLE);

            if (readyCount > 0 && almostCount > 0) {
                textCount.setText(readyCount + (readyCount == 1 ? " recipe" : " recipes") + " ready to cook • " + almostCount + " almost ready");
            } else if (readyCount > 0) {
                textCount.setText("You can make " + readyCount + (readyCount == 1 ? " recipe" : " recipes") + " right now");
            } else {
                textCount.setText("Almost ready recipes (add missing ingredients to complete):");
            }
        }
    }
}