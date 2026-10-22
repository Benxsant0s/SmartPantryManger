package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Shows the full ingredient list and method for one recipe.
 * The recipe id arrives through an Intent extra called "recipe_id".
 */
public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Recipe");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int recipeId = getIntent().getIntExtra("recipe_id", -1);
        Recipe recipe = new DatabaseHelper(this).getRecipeById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView textName = findViewById(R.id.textRecipeName);
        TextView textIngredients = findViewById(R.id.textIngredients);
        TextView textSteps = findViewById(R.id.textSteps);

        textName.setText(recipe.getName());

        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            double q = ing.getQuantity();
            String qty = (q == Math.floor(q)) ? String.valueOf((int) q) : String.valueOf(q);
            ingredients.append("\u2022 ")
                    .append(qty).append(" ").append(ing.getUnit())
                    .append(" ").append(ing.getName()).append("\n");
        }
        textIngredients.setText(ingredients.toString().trim());
        textSteps.setText(recipe.getSteps());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}