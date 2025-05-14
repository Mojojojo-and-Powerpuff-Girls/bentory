package com.example.bentory_app.subcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartModel> cartItems;
    private OnStockChangedListener stockChangedListener;

    public CartAdapter(List<CartModel> cartItems, OnStockChangedListener listener) {
        this.cartItems = cartItems;
        this.stockChangedListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, size, price;
        public EditText quantity;
        public ImageButton addBtn, subtractBtn, removeBtn;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            size = view.findViewById(R.id.item_size);
            quantity = view.findViewById(R.id.item_quantity);
            price = view.findViewById(R.id.item_price);
            addBtn = view.findViewById(R.id.add_btn);
            subtractBtn = view.findViewById(R.id.subtract_btn);
            removeBtn = view.findViewById(R.id.remove_btn);
        }
    }

    // An interface for handling stock changed actions
    public interface OnStockChangedListener {
        void onStockChanged();
    }

    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_cart_layout, parent, false); // Adjust XML file name if needed
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CartModel item = cartItems.get(position);

        holder.name.setText(item.getName());
        holder.size.setText(item.getSize());
        holder.quantity.setText(String.valueOf(item.getQuantity()));
        holder.quantity.setSelection(holder.quantity.getText().length()); // Place cursor at end
        holder.price.setText(String.format("₱ %.2f", item.getPrice()));

        // Add button functionality
        holder.addBtn.setOnClickListener(v -> {
            if (item.getLinkedProduct().getQuantity() > 0) {
                item.setQuantity(item.getQuantity() + 1);
                item.getLinkedProduct().setQuantity(item.getLinkedProduct().getQuantity() - 1);
                stockChangedListener.onStockChanged();
                notifyItemChanged(position);
            } else {
                Toast.makeText(v.getContext(), "No more stock!", Toast.LENGTH_SHORT).show();
            }
        });

        // Subtract button functionality
        holder.subtractBtn.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                item.getLinkedProduct().setQuantity(item.getLinkedProduct().getQuantity() + 1);
                stockChangedListener.onStockChanged();
                notifyItemChanged(position);
            }
        });

        // Remove item from cart
        holder.removeBtn.setOnClickListener(v -> {
            ProductModel linkedProduct = item.getLinkedProduct();
            /// Restore the quantity to product's stock
            linkedProduct.setQuantity(linkedProduct.getQuantity() + item.getQuantity());

            // Proceeds to remove item from cart
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());

            // Notify parent that stock has changed (so selling list updates too)
            stockChangedListener.onStockChanged();
        });

        // Manual typing of quantity
        holder.quantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateQuantity(holder.quantity, item, position);
            }
        });

        holder.quantity.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                // Optionally: manually clear focus to trigger onFocusChangeListener
                holder.quantity.clearFocus();
                updateQuantity(holder.quantity, item, position);
                return true;
            }
            return false;
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // Method for the manual typing of quantity
    private void updateQuantity(EditText quantityField, CartModel item, int position) {
        ProductModel product = item.getLinkedProduct();
        String input = quantityField.getText().toString();
        int currentQuantity = item.getQuantity();

        try {
            int newQuantity = Integer.parseInt(input);
            int diff = newQuantity - currentQuantity;

            if (newQuantity <= 0) {
                quantityField.setText(String.valueOf(currentQuantity)); // Invalid quantity
                return;
            }

            if (diff > 0) {
                if (product.getQuantity() >= diff) {
                    item.setQuantity(newQuantity);
                    product.setQuantity(product.getQuantity() - diff);
                } else {
                    Toast.makeText(quantityField.getContext(), "Not enough stock!", Toast.LENGTH_SHORT).show();
                    quantityField.setText(String.valueOf(currentQuantity));
                    return;
                }
            } else if (diff < 0) {
                item.setQuantity(newQuantity);
                product.setQuantity(product.getQuantity() + (-diff)); // return stock
            }

            if (stockChangedListener != null) stockChangedListener.onStockChanged();
            notifyItemChanged(position);

        } catch (NumberFormatException e) {
            quantityField.setText(String.valueOf(currentQuantity)); // Revert if invalid
        }
    }

}
