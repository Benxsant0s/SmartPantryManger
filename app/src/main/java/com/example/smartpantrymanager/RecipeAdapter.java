package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.List;

/**
 * Adapter that shows a list of recipe matches (name, ingredients, and match status)
 * in the Suggested Recipes screen.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<RecipeMatcher.RecipeMatch> matches;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<RecipeMatcher.RecipeMatch> matches, OnRecipeClickListener listener) {
        this.matches = matches;
        this.listener = listener;
    }

    public void setMatches(List<RecipeMatcher.RecipeMatch> newMatches) {
        this.matches = newMatches;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeMatcher.RecipeMatch match = matches.get(position);
        Recipe recipe = match.getRecipe();

        holder.textName.setText(recipe.getName());

        // Build a short summary such as "egg, butter, salt"
        StringBuilder summary = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(ing.getName());
        }
        holder.textIngredients.setText("Uses: " + summary);

        if (match.isCanMake()) {
            holder.textStatus.setText("Ready to cook - tap for method");
            holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_primary));
        } else {
            List<String> missing = match.getMissingIngredients();
            String missingStr;
            if (missing.size() > 3) {
                missingStr = missing.get(0) + ", " + missing.get(1) + " + " + (missing.size() - 2) + " more";
            } else {
                missingStr = String.join(", ", missing);
            }
            holder.textStatus.setText("Missing: " + missingStr);
            holder.textStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange_accent));
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textIngredients, textStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textIngredients = itemView.findViewById(R.id.textRecipeIngredients);
            textStatus = itemView.findViewById(R.id.textRecipeStatus);
        }
    }
}