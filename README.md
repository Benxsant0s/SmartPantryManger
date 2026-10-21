# Smart Pantry Manager

A Java Android application that helps reduce food waste. The user records the ingredients they already have at home (their "pantry"), and the app suggests only the recipes they can cook **right now**, using strictly those ingredients. No shopping trip required.

Built for **Mobile App Development 700** (Richfield Graduate Institute of Technology), Practical Assignment.

## The core idea: strict matching

A recipe is shown as "suggested" **only if every single ingredient it needs is in the pantry, in at least the required quantity**.

- A recipe needing 5 ingredients when the pantry has 4 is **not** suggested.
- "Almost there" recipes are excluded.
- Matching copes with simple real-world differences:
  - Case and plurals (`Tomatoes` matches `tomato`, `Eggs` matches `egg`)
  - Unit conversion within the same type (`0.5 kg` satisfies `500 g`, `1 cup` equals `240 ml`)
  - Duplicate pantry entries are added together

The logic lives in `RecipeMatcher.java`.

## Features

- **Pantry management:** add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- **Pantry list:** RecyclerView with a custom adapter, bound to the database
- **18 pre-loaded recipes** seeded on first run
- **Suggested Recipes screen:** shows only recipes the pantry can fully cover
- **Recipe Detail screen:** full ingredient list and method
- **Empty-state message** when no recipes match
- **Input validation** on the ingredient form (required name, valid positive quantity)
- **Toolbar navigation** between screens
- **Settings screen** (preferences saved with SharedPreferences)
- Data persists after the app is closed and reopened

Out of scope by design: no maps, no GPS or location services, no payments.

## Database choice: SQLite

I chose **SQLite** (using `SQLiteOpenHelper`) because:

- It runs locally on the device, so the app works offline with no accounts, servers or setup.
- It is the approach covered in the module's persistent data chapter.
- It fully supports Create, Read, Update and Delete, and the data survives app restarts.
- The data is small and relational (pantry items, recipes, recipe ingredients), which suits SQL tables.

### Data model

| Table | Columns |
|---|---|
| `pantry_item` | id (PK), name, quantity, unit, expiry_date |
| `recipe` | id (PK), name, steps |
| `recipe_ingredient` | id (PK), recipe_id (FK to recipe), name, quantity, unit |

## Screens

1. Pantry List
2. Add / Edit Ingredient
3. Suggested Recipes
4. Recipe Detail
5. Settings

## Project structure

```
app/src/main/java/com/example/smartpantrymanger/
  PantryActivity.java              Main screen, pantry list
  AddEditIngredientActivity.java   Add/edit form with validation
  SuggestedRecipesActivity.java    Strictly matched recipes
  RecipeDetailActivity.java        Ingredients and method
  PantryAdapter.java               RecyclerView adapter for pantry items
  RecipeAdapter.java               RecyclerView adapter for recipes
  RecipeMatcher.java               Strict-matching logic
  database/DatabaseHelper.java     SQLite creation, CRUD, seed data
  model/                           PantryItem, Recipe, RecipeIngredient
```

## Setup and run instructions

**Requirements:** Android Studio (recent version), JDK bundled with Android Studio, and either an emulator or an Android phone.

1. Clone the repository:
   ```
   git clone <YOUR-REPOSITORY-URL>
   ```
2. Open Android Studio and choose **File > Open**, then select the cloned project folder.
3. Wait for **Gradle sync** to finish (internet connection needed the first time).
4. Create an emulator in **Tools > Device Manager** (for example Pixel 6, API 34), or connect a phone with USB debugging turned on.
5. Press the green **Run** button.
6. On first launch the database and the 18 recipes are created automatically.

### Try the strict-matching rule

1. Open **Suggested Recipes** from the toolbar. With an empty pantry you see a "no recipes match" message.
2. Add `Egg` (2 pcs) and `Butter` (10 g). **Scrambled Eggs** does not appear yet (one ingredient missing).
3. Add `Salt` (1 tsp). **Scrambled Eggs** now appears.
4. Delete the salt. The recipe disappears again.

## Technologies

- Java (no Kotlin)
- Android Studio
- SQLite with SQLiteOpenHelper
- RecyclerView, custom adapters, Intents, Material components

## Author

- Name: YOUR NAME
- Student number: YOUR STUDENT NUMBER
- Module: Mobile App Development 700
