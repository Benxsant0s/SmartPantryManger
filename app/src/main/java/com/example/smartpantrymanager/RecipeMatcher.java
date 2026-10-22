package com.example.smartpantrymanager;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class RecipeMatcher {

    /**
     * Normalises ingredient names for matching:
     * - Trims whitespace and converts to lowercase
     * - Strips common adjectives for broader matching ("fresh", "table", "extra virgin", etc.)
     * - Handles plural endings ("ies" -> "y", "oes" -> "o", "s" -> "")
     */
    public static String normaliseName(String raw) {
        if (raw == null) return "";
        String n = raw.trim().toLowerCase();

        n = n.replace("extra virgin ", "")
             .replace("fresh ", "")
             .replace("raw ", "")
             .replace("cooked ", "")
             .replace("table ", "")
             .trim();

        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";
        }
        if (n.endsWith("oes") && n.length() > 4) {
            return n.substring(0, n.length() - 2);
        }
        if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 2) {
            return n.substring(0, n.length() - 1);
        }
        return n;
    }

    /**
     * Checks if a pantry item name matches a required recipe ingredient name.
     * Handles exact match, plurals, and partial word matching (e.g., "olive oil" <-> "oil").
     */
    public static boolean isNameMatch(String pantryName, String recipeIngredientName) {
        String p = normaliseName(pantryName);
        String r = normaliseName(recipeIngredientName);

        if (p.isEmpty() || r.isEmpty()) {
            return false;
        }

        if (p.equals(r)) {
            return true;
        }

        // Whole word check (e.g. "olive oil" matches "oil", "cheddar cheese" matches "cheese")
        return containsWord(p, r) || containsWord(r, p);
    }

    private static boolean containsWord(String container, String target) {
        if (target.isEmpty() || container.isEmpty()) return false;
        String[] words = container.split("\\s+");
        for (String w : words) {
            if (w.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a quantity and unit to a standardized base measure.
     * Base units:
     * - Mass: grams (g) [1 kg = 1000g]
     * - Volume: milliliters (ml) [1 l = 1000ml, 1 tbsp = 15ml, 1 tsp = 5ml, 1 cup = 240ml]
     * - Count / Pieces: pcs [1 pcs = 1]
     */
    public static double toBaseQuantity(double quantity, String unit) {
        if (unit == null) return quantity;
        switch (unit.trim().toLowerCase()) {
            case "kg":
                return quantity * 1000;
            case "l":
                return quantity * 1000;
            case "tbsp":
                return quantity * 15;
            case "tsp":
                return quantity * 5;
            case "cup":
                return quantity * 240;
            case "g":
            case "ml":
            case "pcs":
            default:
                return quantity;
        }
    }

    /**
     * Checks if the pantry has enough quantity for a required recipe ingredient.
     * Every required ingredient in the recipe must be present in the pantry in sufficient quantity.
     */
    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        if (recipe == null || recipe.getIngredients() == null) return false;
        if (pantry == null || pantry.isEmpty()) return false;

        for (RecipeIngredient need : recipe.getIngredients()) {
            double requiredBase = toBaseQuantity(need.getQuantity(), need.getUnit());
            double totalAvailable = 0;
            boolean foundIngredientInPantry = false;

            for (PantryItem p : pantry) {
                if (p.getQuantity() <= 0) continue;

                if (isNameMatch(p.getName(), need.getName())) {
                    foundIngredientInPantry = true;
                    double availableBase = toBaseQuantity(p.getQuantity(), p.getUnit());
                    totalAvailable += availableBase;
                }
            }

            if (!foundIngredientInPantry) {
                return false; // Ingredient missing completely
            }

            // Allow tiny floating point tolerance when comparing quantities
            if (totalAvailable < requiredBase - 0.000001) {
                return false; // Not enough quantity
            }
        }

        return true;
    }

    /**
     * Returns all recipes from allRecipes that can be fully cooked with the pantry items.
     */
    public static List<Recipe> getSuggested(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        if (allRecipes == null || pantry == null) return result;

        for (Recipe r : allRecipes) {
            if (canMake(r, pantry)) {
                result.add(r);
            }
        }
        return result;
    }
}