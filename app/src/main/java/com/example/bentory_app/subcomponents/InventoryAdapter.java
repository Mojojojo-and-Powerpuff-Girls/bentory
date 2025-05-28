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

    private Context context;
    private List<ProductModel> productList;
    private OnProductClickListener listener;
    private boolean deleteMode = false;
    private Set<String> selectedItems = new HashSet<>();

    public interface OnProductClickListener {
        void onProductClick(ProductModel product);
    }

    public InventoryAdapter(Context context, List<ProductModel> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_product_layout, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        holder.name.setText(product.getName());
        holder.quantity.setText("Quantity: " + product.getQuantity());
        holder.sale_price.setText(String.format("₱ %.2f", product.getSale_Price()));

        // Step 1: Show or hide checkbox based on delete mode
        // WHY: In delete mode, checkboxes allow users to select items for deletion.
        //Outside delete mode, we hide checkboxes for a cleaner UI.
        if (deleteMode) {
            holder.checkboxItemDlt.setVisibility(View.VISIBLE);
        }
        else {
            holder.checkboxItemDlt.setVisibility(View.INVISIBLE);
        }

        // Step 2: Clear any previous checkbox listeners
        // WHY: RecyclerView reuses view holders. If we don't clear the listener,
        // it can be triggered incorrectly during recycling.
        holder.checkboxItemDlt.setOnCheckedChangeListener(null);

        // Step 3: Set checkbox state based on whether the item is selected
        // WHY: Ensures that selected items remain checked when views are recycled.
        holder.checkboxItemDlt.setChecked(selectedItems.contains(product.getId()));

        // Step 4: Handle checkbox state changes (check/uncheck)
        // WHY: Adds or removes the product ID from the selectedItems set accordingly.
        holder.checkboxItemDlt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String id = product.getId();
            if (id != null && !id.trim().isEmpty()) {
                Log.d("InventoryAdapter", "Checkbox toggled for id=" + id + ", checked=" + isChecked);
                if (isChecked) {
                    selectedItems.add(id);
                } else {
                    selectedItems.remove(id);
                }
            } else {
                Log.e("InventoryAdapter", "Invalid product ID when toggling checkbox");
            }
        });

        // Step 5: Enable or disable item click behavior based on delete mode
        // WHY: In delete mode, item clicks should be disabled to avoid triggering normal item actions.
        if (!deleteMode) {
            holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
        else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // 1: Method to toggle delete mode on or off
    // WHY: Activates delete mode (showing checkboxes and enabling multi-select),
    // and deactivates it (clearing selections and hiding checkboxes).
    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;

        // Step 1: If exiting delete mode, clear any selected items
        // WHY: We don't want stale selections left over when the user isn't deleting anymore.
        if (!deleteMode) {
            selectedItems.clear();
        }

        // Step 3: Refresh the entire RecyclerView UI
        // WHY: To show or hide checkboxes depending on the mode.
        notifyDataSetChanged();
    }

    // 2: Getter method to check if we're currently in delete mode
    // WHY: Used externally (e.g., in activity) to determine the current mode state.
    public boolean getDeleteMode() {
        return deleteMode;
    }

    // 3: Getter method for selected item IDs
    // WHY: Allows other classes (like ViewModel or Activity) to access selected items for deletion.
    public Set<String> getSelectedItems() {
        return selectedItems;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, sale_price;
        CheckBox checkboxItemDlt;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.productQuantity);
            sale_price = itemView.findViewById(R.id.productSalePrice);
            checkboxItemDlt = itemView.findViewById(R.id.itemCheckBox); // Initialize
        }
    }

    // COMMENT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public void updateData(List<ProductModel> newData) {
        this.productList = newData;
        notifyDataSetChanged();
    }
}
