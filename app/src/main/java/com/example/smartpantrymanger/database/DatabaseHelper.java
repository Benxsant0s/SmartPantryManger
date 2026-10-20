package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry_item ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");

        db.execSQL("CREATE TABLE recipe ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE recipe_ingredient ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredient");
        db.execSQL("DROP TABLE IF EXISTS recipe");
        db.execSQL("DROP TABLE IF EXISTS pantry_item");
        onCreate(db);
    }


    public long insertItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());
        return db.insert("pantry_item", null, values);
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM pantry_item ORDER BY name", null);
        while (c.moveToNext()) {
            list.add(cursorToItem(c));
        }
        c.close();
        return list;
    }

    public PantryItem getItemById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM pantry_item WHERE id = ?",
                new String[]{String.valueOf(id)});
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = cursorToItem(c);
        }
        c.close();
        return item;
    }

    public int updateItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());
        return db.update("pantry_item", values, "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public int deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("pantry_item", "id = ?", new String[]{String.valueOf(id)});
    }


    private PantryItem cursorToItem(Cursor c) {
        return new PantryItem(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }



    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM recipe ORDER BY name", null);
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            String name = c.getString(c.getColumnIndexOrThrow("name"));
            String steps = c.getString(c.getColumnIndexOrThrow("steps"));
            recipes.add(new Recipe(id, name, steps, getIngredientsForRecipe(db, id)));
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipeById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM recipe WHERE id = ?",
                new String[]{String.valueOf(id)});
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(
                    id,
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")),
                    getIngredientsForRecipe(db, id));
        }
        c.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT name, quantity, unit FROM recipe_ingredient "
                + "WHERE recipe_id = ?", new String[]{String.valueOf(recipeId)});
        while (c.moveToNext()) {
            list.add(new RecipeIngredient(c.getString(0), c.getDouble(1), c.getString(2)));
        }
        c.close();
        return list;
    }



    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Whisk the eggs with salt.\n2. Melt the butter in a pan.\n3. Add the eggs and stir gently until just set.",
                new String[][]{{"egg", "2", "pcs"}, {"butter", "10", "g"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until tender, then drain.\n2. Fry the garlic and tomatoes in olive oil.\n3. Toss the pasta through the sauce.",
                new String[][]{{"pasta", "200", "g"}, {"tomato", "3", "pcs"}, {"olive oil", "2", "tbsp"}, {"garlic", "2", "pcs"}});

        addRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs.\n2. Melt butter in a pan and pour in the eggs.\n3. Add the cheese, fold and serve.",
                new String[][]{{"egg", "3", "pcs"}, {"cheese", "50", "g"}, {"butter", "10", "g"}});

        addRecipe(db, "Pancakes",
                "1. Mix flour, milk and egg into a smooth batter.\n2. Heat a little butter in a pan.\n3. Cook spoonfuls of batter until golden on both sides.",
                new String[][]{{"flour", "150", "g"}, {"milk", "250", "ml"}, {"egg", "1", "pcs"}, {"butter", "10", "g"}});

        addRecipe(db, "Garlic Butter Toast",
                "1. Mash the garlic into the butter.\n2. Spread on the bread.\n3. Toast until golden.",
                new String[][]{{"bread", "4", "pcs"}, {"butter", "30", "g"}, {"garlic", "2", "pcs"}});

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Place cheese between the slices.\n3. Fry on both sides until the cheese melts.",
                new String[][]{{"bread", "2", "pcs"}, {"cheese", "40", "g"}, {"butter", "10", "g"}});

        addRecipe(db, "Plain Rice",
                "1. Rinse the rice.\n2. Boil with water and salt, covered, for about 15 minutes.\n3. Rest for 5 minutes and fluff with a fork.",
                new String[][]{{"rice", "1", "cup"}, {"water", "500", "ml"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Egg Fried Rice",
                "1. Heat the oil in a pan.\n2. Scramble the eggs, then add the cooked rice.\n3. Season with soy sauce and stir-fry for 3 minutes.",
                new String[][]{{"rice", "2", "cup"}, {"egg", "2", "pcs"}, {"soy sauce", "2", "tbsp"}, {"oil", "1", "tbsp"}});

        addRecipe(db, "Tomato Soup",
                "1. Fry the onion in butter.\n2. Add the tomatoes and water and simmer for 20 minutes.\n3. Blend until smooth and season with salt.",
                new String[][]{{"tomato", "6", "pcs"}, {"onion", "1", "pcs"}, {"butter", "20", "g"}, {"water", "500", "ml"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with butter and milk.\n3. Season with salt.",
                new String[][]{{"potato", "4", "pcs"}, {"butter", "30", "g"}, {"milk", "50", "ml"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Roast Potatoes",
                "1. Heat the oven to 200 C.\n2. Toss cut potatoes in oil and salt.\n3. Roast for 40 minutes, turning once.",
                new String[][]{{"potato", "5", "pcs"}, {"oil", "3", "tbsp"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Banana Smoothie",
                "1. Peel the bananas.\n2. Blend with milk and honey until smooth.\n3. Serve cold.",
                new String[][]{{"banana", "2", "pcs"}, {"milk", "300", "ml"}, {"honey", "1", "tbsp"}});

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas and mix with the eggs.\n2. Add the flour and stir to a batter.\n3. Fry spoonfuls until golden on both sides.",
                new String[][]{{"banana", "2", "pcs"}, {"egg", "2", "pcs"}, {"flour", "100", "g"}});

        addRecipe(db, "Onion and Cheese Pasta",
                "1. Boil the pasta and drain.\n2. Fry the onion in butter until soft.\n3. Stir in the pasta and cheese until melted.",
                new String[][]{{"pasta", "200", "g"}, {"onion", "2", "pcs"}, {"cheese", "60", "g"}, {"butter", "20", "g"}});

        addRecipe(db, "Peanut Butter Sandwich",
                "1. Spread peanut butter on one slice of bread.\n2. Top with the second slice.\n3. Cut in half.",
                new String[][]{{"bread", "2", "pcs"}, {"peanut butter", "2", "tbsp"}});

        addRecipe(db, "Porridge",
                "1. Combine oats, milk and a pinch of salt in a pot.\n2. Cook on low heat, stirring, for 5 minutes.\n3. Serve with honey.",
                new String[][]{{"oat", "50", "g"}, {"milk", "250", "ml"}, {"honey", "1", "tbsp"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Boiled Eggs on Toast",
                "1. Boil the eggs for 7 minutes.\n2. Toast the bread and butter it.\n3. Serve the eggs on the toast with salt.",
                new String[][]{{"egg", "2", "pcs"}, {"bread", "2", "pcs"}, {"butter", "10", "g"}, {"salt", "1", "tsp"}});

        addRecipe(db, "Garlic Rice",
                "1. Fry the garlic in butter.\n2. Add the cooked rice and salt.\n3. Stir for 2 minutes and serve.",
                new String[][]{{"rice", "2", "cup"}, {"garlic", "3", "pcs"}, {"butter", "15", "g"}, {"salt", "1", "tsp"}});
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, String[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert("recipe", null, recipeValues);

        for (String[] ing : ingredients) {
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", ing[0]);
            iv.put("quantity", Double.parseDouble(ing[1]));
            iv.put("unit", ing[2]);
            db.insert("recipe_ingredient", null, iv);
        }
    }
}