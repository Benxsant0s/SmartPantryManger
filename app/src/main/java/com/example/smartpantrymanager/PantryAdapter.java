package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

/**
 * Custom adapter that binds a list of PantryItem objects
 * to the item_pantry layout inside a RecyclerView.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    // Lets the Activity decide what happens on edit/delete
    public interface OnItemActionListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items;
    private final OnItemActionListener listener;

    public PantryAdapter(List<PantryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    // Replaces the data and refreshes the list
    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.textName.setText(item.getName());

        // Show whole numbers without ".0"
        double q = item.getQuantity();
        String qty = (q == Math.floor(q)) ? String.valueOf((int) q) : String.valueOf(q);
        holder.textQuantity.setText(qty + " " + item.getUnit());

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.isEmpty()) {
            holder.textExpiry.setText("No expiry date");
        } else {
            holder.textExpiry.setText("Expires: " + expiry);
        }

        holder.buttonEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Holds references to the views of one row
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textQuantity, textExpiry;
        ImageButton buttonEdit, buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}