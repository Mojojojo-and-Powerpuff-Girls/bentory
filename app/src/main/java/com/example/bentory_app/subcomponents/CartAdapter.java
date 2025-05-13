package com.example.bentory_app.subcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.CartModel;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartModel> cartItems;

    public CartAdapter(List<CartModel> cartItems) {
        this.cartItems = cartItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, size, quantity, price;
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
        holder.price.setText(item.getPrice());

        // Add button functionality
        holder.addBtn.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
        });

        // Subtract button functionality
        holder.subtractBtn.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
            }
        });

        // Remove item from cart
        holder.removeBtn.setOnClickListener(v -> {
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}
