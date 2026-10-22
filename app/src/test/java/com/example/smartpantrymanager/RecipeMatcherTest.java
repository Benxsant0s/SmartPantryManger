package com.example.smartpantrymanager;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class RecipeMatcherTest {

    @Test
    public void testNormaliseName() {
        assertEquals("egg", RecipeMatcher.normaliseName("Eggs"));
        assertEquals("tomato", RecipeMatcher.normaliseName("Tomatoes"));
        assertEquals("potato", RecipeMatcher.normaliseName("Potatoes"));
        assertEquals("berry", RecipeMatcher.normaliseName("Berries"));
        assertEquals("olive oil", RecipeMatcher.normaliseName("Extra Virgin Olive Oil"));
        assertEquals("salt", RecipeMatcher.normaliseName("Table Salt"));
    }

    @Test
    public void testIsNameMatch() {
        assertTrue(RecipeMatcher.isNameMatch("Eggs", "egg"));
        assertTrue(RecipeMatcher.isNameMatch("Tomatoes", "tomato"));
        assertTrue(RecipeMatcher.isNameMatch("Olive Oil", "oil"));
        assertTrue(RecipeMatcher.isNameMatch("Cheddar Cheese", "cheese"));
        assertFalse(RecipeMatcher.isNameMatch("Eggplant", "egg"));
    }

    @Test
    public void testCanMake_exactMatch() {
        List<RecipeIngredient> ingredients = Arrays.asList(
                new RecipeIngredient("egg", 2, "pcs"),
                new RecipeIngredient("butter", 10, "g"),
                new RecipeIngredient("salt", 1, "tsp")
        );
        Recipe scrambledEggs = new Recipe(1, "Scrambled Eggs", "Steps...", ingredients);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem(1, "Egg", 2, "pcs", null),
                new PantryItem(2, "Butter", 10, "g", null),
                new PantryItem(3, "Salt", 100, "g", null)
        );

        assertTrue(RecipeMatcher.canMake(scrambledEggs, pantry));
    }

    @Test
    public void testCanMake_missingIngredient() {
        List<RecipeIngredient> ingredients = Arrays.asList(
                new RecipeIngredient("egg", 2, "pcs"),
                new RecipeIngredient("butter", 10, "g"),
                new RecipeIngredient("salt", 1, "tsp")
        );
        Recipe scrambledEggs = new Recipe(1, "Scrambled Eggs", "Steps...", ingredients);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem(1, "Egg", 2, "pcs", null),
                new PantryItem(2, "Butter", 10, "g", null)
        );

        assertFalse(RecipeMatcher.canMake(scrambledEggs, pantry));

        RecipeMatcher.RecipeMatch match = RecipeMatcher.getMatchDetails(scrambledEggs, pantry);
        assertEquals(2, match.getMatchedCount());
        assertEquals(3, match.getTotalCount());
        assertEquals(1, match.getMissingIngredients().size());
        assertTrue(match.getMissingIngredients().get(0).contains("salt"));
    }

    @Test
    public void testCanMake_insufficientQuantity() {
        List<RecipeIngredient> ingredients = Collections.singletonList(
                new RecipeIngredient("egg", 2, "pcs")
        );
        Recipe scrambledEggs = new Recipe(1, "Scrambled Eggs", "Steps...", ingredients);

        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem(1, "Egg", 1, "pcs", null)
        );

        assertFalse(RecipeMatcher.canMake(scrambledEggs, pantry));
    }

    @Test
    public void testCanMake_multiplePantryEntriesSum() {
        List<RecipeIngredient> ingredients = Collections.singletonList(
                new RecipeIngredient("egg", 3, "pcs")
        );
        Recipe scrambledEggs = new Recipe(1, "Scrambled Eggs", "Steps...", ingredients);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem(1, "Egg", 1, "pcs", null),
                new PantryItem(2, "Eggs", 2, "pcs", null)
        );

        assertTrue(RecipeMatcher.canMake(scrambledEggs, pantry));
    }
}