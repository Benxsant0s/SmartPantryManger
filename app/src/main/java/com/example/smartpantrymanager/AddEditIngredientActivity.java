package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private TextInputLayout tilName, tilQuantity, tilUnit;
    private TextInputEditText editName, editQuantity, editExpiry;
    private AutoCompleteTextView autoUnit;
    private DatabaseHelper db;
    private int itemId = -1; // -1 means "add mode"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilUnit = findViewById(R.id.tilUnit);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        autoUnit = findViewById(R.id.autoUnit);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonClearDate = findViewById(R.id.buttonClearDate);

        // Fill the unit dropdown (default: g)
        autoUnit.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, UNITS));
        autoUnit.setText(UNITS[0], false);

        // Read the Intent extra to decide between add and edit mode
        itemId = getIntent().getIntExtra("item_id", -1);
        if (itemId != -1) {
            setTitle("Edit ingredient");
            loadItemForEditing();
        } else {
            setTitle("Add ingredient");
        }

        editExpiry.setOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> editExpiry.setText(""));
        buttonSave.setOnClickListener(v -> saveItem());
    }

    // Edit mode: load the existing record and show it in the form
    private void loadItemForEditing() {
        PantryItem item = db.getItemById(itemId);
        if (item == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        editName.setText(item.getName());

        double q = item.getQuantity();
        editQuantity.setText(q == Math.floor(q) ? String.valueOf((int) q) : String.valueOf(q));

        autoUnit.setText(item.getUnit(), false);

        if (item.getExpiryDate() != null) {
            editExpiry.setText(item.getExpiryDate());
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            // Stored as yyyy-MM-dd text
            editExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveItem() {
        String name = String.valueOf(editName.getText()).trim();
        String quantityText = String.valueOf(editQuantity.getText()).trim();
        String unit = autoUnit.getText().toString().trim();
        String expiry = String.valueOf(editExpiry.getText()).trim();

        // Clear any previous errors
        tilName.setError(null);
        tilQuantity.setError(null);
        tilUnit.setError(null);

        // ---------- Input validation ----------
        if (name.isEmpty()) {
            tilName.setError("Name is required");
            return;
        }
        if (name.length() < 2) {
            tilName.setError("Name must be at least 2 characters");
            return;
        }
        if (quantityText.isEmpty()) {
            tilQuantity.setError("Quantity is required");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            tilQuantity.setError("Enter a valid number");
            return;
        }
        if (quantity <= 0) {
            tilQuantity.setError("Quantity must be greater than 0");
            return;
        }

        boolean validUnit = false;
        for (String u : UNITS) {
            if (u.equals(unit)) {
                validUnit = true;
                break;
            }
        }
        if (!validUnit) {
            tilUnit.setError("Choose a unit from the list");
            return;
        }

        // Empty expiry is saved as null (it is optional)
        String expiryValue = expiry.isEmpty() ? null : expiry;

        if (itemId == -1) {
            // CREATE
            db.insertItem(new PantryItem(0, name, quantity, unit, expiryValue));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            // UPDATE
            db.updateItem(new PantryItem(itemId, name, quantity, unit, expiryValue));
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // return to the pantry list, which reloads in onResume()
    }

    // Back arrow in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}