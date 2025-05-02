package com.example.bentory_app.subcomponents;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductModel> productList;
    private OnProductClickListener listener;

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
        holder.sale_price.setText(String.format("PHP %.2f", product.getSalePrice()));


        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, sale_price;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.productQuantity);
            sale_price = itemView.findViewById(R.id.productSalePrice);
        }
    }
}
