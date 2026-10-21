package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.Calendar;
import java.util.Locale;

/**
 * Screen used to add a new pantry item or edit an existing one.
 * If an "item_id" extra is passed in the Intent, the screen is in edit mode.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private EditText editName, editQuantity, editExpiry;
    private Spinner spinnerUnit;
    private DatabaseHelper db;
    private int itemId = -1; // -1 means "add mode"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = new DatabaseHelper(this);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonClearDate = findViewById(R.id.buttonClearDate);

        // Fill the unit spinner
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinnerUnit.setAdapter(unitAdapter);

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

        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(item.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
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
        String name = editName.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        String expiry = editExpiry.getText().toString().trim();

        // ---------- Input validation ----------
        if (name.isEmpty()) {
            editName.setError("Name is required");
            editName.requestFocus();
            return;
        }
        if (name.length() < 2) {
            editName.setError("Name must be at least 2 characters");
            editName.requestFocus();
            return;
        }
        if (quantityText.isEmpty()) {
            editQuantity.setError("Quantity is required");
            editQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            editQuantity.setError("Enter a valid number");
            editQuantity.requestFocus();
            return;
        }
        if (quantity <= 0) {
            editQuantity.setError("Quantity must be greater than 0");
            editQuantity.requestFocus();
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
}