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

    private List<ProductModel> productList = new ArrayList<>();

    public void setProductList(List<ProductModel> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView sellingName, sellingSize, sellingPrice, sellingStock;
        ImageButton sellingAddBtn;

        // ViewHolder class that holds the views for each product item in the list
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            // Connect the XML views to Java variables
            sellingName = itemView.findViewById(R.id.sellingProductName);
            sellingSize = itemView.findViewById(R.id.sellingProductSize);
            sellingPrice = itemView.findViewById(R.id.sellingProductPrice);
            sellingStock = itemView.findViewById(R.id.sellingProductStock);
            sellingAddBtn = itemView.findViewById(R.id.sellingProductAdd);
        }

        // This method sets the data from the product model to the views
        public void bind(ProductModel product) {
            sellingName.setText(product.getName());
            sellingSize.setText(product.getSize());
            sellingPrice.setText(String.format("₱ %.2f", product.getSale_Price()));
            sellingStock.setText(String.valueOf(product.getQuantity()));
        }
    }


    // Called when a new ViewHolder needs to be created
    @NonNull
    @Override
    public SellingProductAdapter.ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single product item from XML
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_avail_selling_product_layout, parent, false);

        // Return a new ViewHolder instance with the inflated view
        return new ProductViewHolder(view);
    }

    // Called when data needs to be shown in the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Get the product at the current position and bind its data to the views
        holder.bind(productList.get(position));
    }

    // Tells the RecyclerView how many items are in the list
    @Override
    public int getItemCount() {
        return productList.size(); // Return the total number of products
    }
}