package com.example.bentory_app.subcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;

import java.util.ArrayList;
import java.util.List;

public class SellingProductAdapter extends RecyclerView.Adapter<SellingProductAdapter.ProductViewHolder> {

    // State
    private List<ProductModel> productList = new ArrayList<>();

    // Listener
    private OnAddToCartClickListener onAddToCartClickListener;

    // 🧱 Constructor accepts listener that defines what happens when an item is added to cart.
    public SellingProductAdapter(OnAddToCartClickListener onAddToCartClickListener) {
        this.onAddToCartClickListener = onAddToCartClickListener;
    }

    // 🔁 Used to update the product list displayed in the RecyclerView.
    public void setProductList(List<ProductModel> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    // 🧊 ViewHolder class holds references to item layout views.
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView sellingName, sellingSize, sellingPrice, sellingStock;
        ImageButton sellingAddBtn, addToCartBtn;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            // Connect layout elements with their corresponding views.
            sellingName = itemView.findViewById(R.id.sellingProductName);
            sellingSize = itemView.findViewById(R.id.sellingProductSize);
            sellingPrice = itemView.findViewById(R.id.sellingProductPrice);
            sellingStock = itemView.findViewById(R.id.sellingProductStock);
            sellingAddBtn = itemView.findViewById(R.id.sellingProductAdd);
            addToCartBtn = itemView.findViewById(R.id.sellingProductAdd);
        }

        // Binds a product's data to the view components.
        public void bind(ProductModel product) {
            sellingName.setText(product.getName());
            sellingSize.setText(product.getSize());
            sellingPrice.setText(String.format("₱ %.2f", product.getSale_Price()));
            sellingStock.setText(String.valueOf(product.getQuantity()));
        }
    }

    // 🎯 Callback interface used to notify when a product is added to cart.
    public interface OnAddToCartClickListener {
        void onAddToCart(ProductModel product);
    }

    // 🏗️ Called when creating new ViewHolder for each row/item.
    @NonNull
    @Override
    public SellingProductAdapter.ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_avail_selling_product_layout, parent, false);
        return new ProductViewHolder(view);
    }

    // 🎨 Bind data to the ViewHolder and set up listeners.
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));

        // When the "Add to Cart" button is clicked:
        holder.addToCartBtn.setOnClickListener(v -> {
            // Check if a listener has been set (to handle the Add to Cart action)
            if (onAddToCartClickListener != null) {
                // Trigger the callback and pass the selected product to whoever is listening
                onAddToCartClickListener.onAddToCart(productList.get(position));
            }
        });
    }

    // 🔢 Return total number of items to display.
    @Override
    public int getItemCount() {
        return productList.size();
    }
}