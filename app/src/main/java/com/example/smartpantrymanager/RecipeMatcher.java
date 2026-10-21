package com.example.smartpantrymanager;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecipeMatcher {

    static String normaliseName(String raw) {
        String n = raw.trim().toLowerCase();
        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";
        }
        if (n.endsWith("oes")) {
            return n.substring(0, n.length() - 2);
        }
        if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 2) {
            return n.substring(0, n.length() - 1);
        }
        return n;
    }

    static String unitType(String unit) {
        switch (unit.trim().toLowerCase()) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return "volume";
            default:
                return "count";
        }
    }

    static double toBase(double quantity, String unit) {
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
            default:
                return quantity; // g, ml, pcs
        }
    }

    private static String key(String name, String unit) {
        return normaliseName(name) + "|" + unitType(unit);
    }

    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        Map<String, Double> available = new HashMap<>();
        for (PantryItem p : pantry) {
            String k = key(p.getName(), p.getUnit());
            double base = toBase(p.getQuantity(), p.getUnit());
            Double current = available.get(k);
            available.put(k, (current == null ? 0 : current) + base);
        }

        for (RecipeIngredient need : recipe.getIngredients()) {
            Double have = available.get(key(need.getName(), need.getUnit()));
            double required = toBase(need.getQuantity(), need.getUnit());

            // Missing ingredient, or not enough of it: recipe is excluded
            if (have == null || have < required - 0.000001) {
                return false;
            }
        }
        return true;
    }

    public static List<Recipe> getSuggested(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : allRecipes) {
            if (canMake(r, pantry)) {
                result.add(r);
            }
        }
        return result;
    }
}