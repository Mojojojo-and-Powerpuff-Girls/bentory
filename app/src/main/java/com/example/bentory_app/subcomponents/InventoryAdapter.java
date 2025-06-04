package com.example.bentory_app.subcomponents;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ProductViewHolder> {

    //State
    private List<ProductModel> productList;
    private boolean deleteMode = false;
    private Set<String> selectedItems = new HashSet<>();
    private Context context;

    // Listener
    private OnProductClickListener listener;

    // 🧩 Interface for handling item clicks outside delete mode.
    public interface OnProductClickListener {
        void onProductClick(ProductModel product);
    }

    // 📦 Constructor
    public InventoryAdapter(Context context, List<ProductModel> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    // 🏗️ Create ViewHolder using your item layout.
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_product_layout, parent, false);
        return new ProductViewHolder(view);
    }

    // ===============================
    // 🎨 Bind and Checkbox of Delete Mode : Bind product data to UI elements and manage checkbox logic. (FUNCTIONALITY)
    // ===============================
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        // Display product details.
        holder.name.setText(product.getName());
        holder.quantity.setText("Quantity: " + product.getQuantity());
        holder.sale_price.setText(String.format("₱ %.2f", product.getSale_Price()));

        // Show/hide checkbox based on delete mode.
        if (deleteMode) {
            holder.checkboxItemDlt.setVisibility(View.VISIBLE);
        }
        else {
            holder.checkboxItemDlt.setVisibility(View.INVISIBLE);
        }

        // Remove previous checkbox listener to prevent unwanted triggers.
        holder.checkboxItemDlt.setOnCheckedChangeListener(null);

        // Update checkbox state (checked if product is selected).
        holder.checkboxItemDlt.setChecked(selectedItems.contains(product.getId()));

        // Handle checkbox toggle action.
        holder.checkboxItemDlt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String id = product.getId();
            if (id != null && !id.trim().isEmpty()) {
                Log.d("InventoryAdapter", "Checkbox toggled for id=" + id + ", checked=" + isChecked);
                if (isChecked) {
                    selectedItems.add(id);      // add to selection.
                } else {
                    selectedItems.remove(id);   // remove from selection.
                }
            } else {
                Log.e("InventoryAdapter", "Invalid product ID when toggling checkbox");
            }
        });

        // Enable/Disable normal click actions based on mode.
        if (!deleteMode) {
            holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
        else {
            holder.itemView.setOnClickListener(null); // disable clicks.
        }
    }

    // 🔢 Get number of items.
    @Override
    public int getItemCount() {
        return productList.size();
    }

    // 🔄 Toggle delete mode (with automatic checkbox display and selection clearing).
    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;

        // Clear selections when exiting delete mode.
        if (!deleteMode) {
            selectedItems.clear();
        }

        // Refresh UI to reflect mode change.
        notifyDataSetChanged();
    }

    // 📍 Check current mode state.
    public boolean getDeleteMode() {
        return deleteMode;
    }

    // 📤 Get list of selected product IDs for deletion.
    public Set<String> getSelectedItems() {
        return selectedItems;
    }

    // 🧱 ViewHolder : holds reference to layout views.
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, sale_price;
        CheckBox checkboxItemDlt;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.productQuantity);
            sale_price = itemView.findViewById(R.id.productSalePrice);
            checkboxItemDlt = itemView.findViewById(R.id.itemCheckBox);
        }
    }

    // 🔁 Called externally to update list data.
    public void updateData(List<ProductModel> newData) {
        this.productList = newData;
        notifyDataSetChanged();
    }
}
