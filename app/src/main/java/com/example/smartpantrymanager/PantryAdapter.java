package com.example.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.model.PantryItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    // Lets the Activity decide what happens on edit/delete
    public interface OnItemActionListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items;
    private final OnItemActionListener listener;
    private boolean highlightExpiring = true; // controlled by the Settings screen

    public PantryAdapter(List<PantryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setHighlightExpiring(boolean highlightExpiring) {
        this.highlightExpiring = highlightExpiring;
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
        Context ctx = holder.itemView.getContext();

        String name = item.getName();
        holder.textName.setText(name);
        holder.textInitial.setText(name.isEmpty() ? "?"
                : name.substring(0, 1).toUpperCase(Locale.getDefault()));

        // Show whole numbers without ".0"
        double q = item.getQuantity();
        String qty = (q == Math.floor(q)) ? String.valueOf((int) q) : String.valueOf(q);
        holder.textQuantity.setText(qty + " " + item.getUnit());

        // Expiry text, coloured red when expired or (if enabled) expiring soon
        int normal = ContextCompat.getColor(ctx, R.color.text_secondary);
        int warn = ContextCompat.getColor(ctx, R.color.expiry_warning);
        String expiry = item.getExpiryDate();

        if (expiry == null || expiry.isEmpty()) {
            holder.textExpiry.setText("No expiry date");
            holder.textExpiry.setTextColor(normal);
        } else {
            long days = daysUntil(expiry);
            if (days != Long.MAX_VALUE && days < 0) {
                holder.textExpiry.setText("Expired on " + expiry);
                holder.textExpiry.setTextColor(warn);
            } else if (highlightExpiring && days <= 3) {
                holder.textExpiry.setText(days == 0 ? "Expires today"
                        : "Expires in " + days + (days == 1 ? " day" : " days"));
                holder.textExpiry.setTextColor(warn);
            } else {
                holder.textExpiry.setText("Expires: " + expiry);
                holder.textExpiry.setTextColor(normal);
            }
        }

        holder.buttonEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    // Whole days from today until the date (yyyy-MM-dd); Long.MAX_VALUE if unreadable
    private static long daysUntil(String dateText) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date date = format.parse(dateText);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return Math.round((date.getTime() - today.getTimeInMillis()) / 86400000.0);
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Holds references to the views of one row
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textInitial, textName, textQuantity, textExpiry;
        ImageButton buttonEdit, buttonDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textInitial = itemView.findViewById(R.id.textInitial);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}