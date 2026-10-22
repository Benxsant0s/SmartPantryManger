package com.example.smartpantrymanager;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RecipeMatcher {

    public static class RecipeMatch {
        private final Recipe recipe;
        private final boolean canMake;
        private final List<String> missingIngredients;
        private final int matchedCount;
        private final int totalCount;

        public RecipeMatch(Recipe recipe, boolean canMake, List<String> missingIngredients, int matchedCount, int totalCount) {
            this.recipe = recipe;
            this.canMake = canMake;
            this.missingIngredients = missingIngredients;
            this.matchedCount = matchedCount;
            this.totalCount = totalCount;
        }

        public Recipe getRecipe() { return recipe; }
        public boolean isCanMake() { return canMake; }
        public List<String> getMissingIngredients() { return missingIngredients; }
        public int getMatchedCount() { return matchedCount; }
        public int getTotalCount() { return totalCount; }
    }

    /**
     * Normalises ingredient names for matching:
     * - Trims whitespace and converts to lowercase
     * - Strips common adjectives ("fresh", "table", "extra virgin", etc.)
     * - Handles plural endings ("ies" -> "y", "oes" -> "o", "s" -> "")
     */
    public static String normaliseName(String raw) {
        if (raw == null) return "";
        String n = raw.trim().toLowerCase(Locale.US);

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
        switch (unit.trim().toLowerCase(Locale.US)) {
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
     * Evaluates a recipe against the current pantry, calculating matched count and missing ingredients.
     */
    public static RecipeMatch getMatchDetails(Recipe recipe, List<PantryItem> pantry) {
        if (recipe == null || recipe.getIngredients() == null) {
            return new RecipeMatch(recipe, false, Collections.emptyList(), 0, 0);
        }

        List<RecipeIngredient> needs = recipe.getIngredients();
        int totalCount = needs.size();
        int matchedCount = 0;
        List<String> missing = new ArrayList<>();

        if (pantry != null && !pantry.isEmpty()) {
            for (RecipeIngredient need : needs) {
                double requiredBase = toBaseQuantity(need.getQuantity(), need.getUnit());
                double totalAvailable = 0;

                for (PantryItem p : pantry) {
                    if (p.getQuantity() <= 0) continue;
                    if (isNameMatch(p.getName(), need.getName())) {
                        totalAvailable += toBaseQuantity(p.getQuantity(), p.getUnit());
                    }
                }

                if (totalAvailable >= requiredBase - 0.000001) {
                    matchedCount++;
                } else if (totalAvailable > 0) {
                    double neededBase = requiredBase - totalAvailable;
                    missing.add(formatQuantity(neededBase, need.getUnit()) + " " + need.getUnit() + " " + need.getName());
                } else {
                    missing.add(formatQuantity(need.getQuantity(), need.getUnit()) + " " + need.getUnit() + " " + need.getName());
                }
            }
        } else {
            for (RecipeIngredient need : needs) {
                missing.add(formatQuantity(need.getQuantity(), need.getUnit()) + " " + need.getUnit() + " " + need.getName());
            }
        }

        boolean canMake = (matchedCount == totalCount && totalCount > 0);
        return new RecipeMatch(recipe, canMake, missing, matchedCount, totalCount);
    }

    private static String formatQuantity(double q, String unit) {
        double displayQty = q;
        if ("kg".equalsIgnoreCase(unit) || "l".equalsIgnoreCase(unit)) {
            displayQty = q / 1000.0;
        } else if ("tbsp".equalsIgnoreCase(unit)) {
            displayQty = q / 15.0;
        } else if ("tsp".equalsIgnoreCase(unit)) {
            displayQty = q / 5.0;
        } else if ("cup".equalsIgnoreCase(unit)) {
            displayQty = q / 240.0;
        }

        if (displayQty == Math.floor(displayQty)) {
            return String.valueOf((int) displayQty);
        }
        return String.format(Locale.US, "%.1f", displayQty);
    }

    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        return getMatchDetails(recipe, pantry).isCanMake();
    }

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

    /**
     * Returns match results for all recipes where the user has at least 1 ingredient,
     * sorted with 100% matched recipes first, followed by partially matched recipes.
     */
    public static List<RecipeMatch> getMatchResults(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<RecipeMatch> results = new ArrayList<>();
        if (allRecipes == null) return results;

        for (Recipe r : allRecipes) {
            RecipeMatch match = getMatchDetails(r, pantry);
            if (match.isCanMake() || match.getMatchedCount() > 0) {
                results.add(match);
            }
        }

        Collections.sort(results, (a, b) -> {
            if (a.isCanMake() != b.isCanMake()) {
                return a.isCanMake() ? -1 : 1;
            }
            return Integer.compare(b.getMatchedCount(), a.getMatchedCount());
        });

        return results;
    }
}