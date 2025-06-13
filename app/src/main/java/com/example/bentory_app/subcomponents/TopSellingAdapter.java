package com.example.bentory_app.subcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.TopSellingModel;

import java.util.List;

public class TopSellingAdapter extends RecyclerView.Adapter<TopSellingAdapter.ViewHolder> {

    // State
    private List<TopSellingModel> productList;

    // 🧱 Constructor to initialize the adapter with a list of products.
    public TopSellingAdapter(List<TopSellingModel> productList) {
        this.productList = productList;
    }

    // 🧊 ViewHolder class to hold the views for each product item.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView size;
        public TextView sold;
        public TextView stock;
        public TextView status;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.product_name);
            size = view.findViewById(R.id.product_size);
            sold = view.findViewById(R.id.product_sold);
            stock = view.findViewById(R.id.product_stock);
            status = view.findViewById(R.id.product_status);
        }
    }

    // 🛠️ Inflate layout and create a new ViewHolder.
    @Override
    public TopSellingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_top_selling_layout, parent, false); // Adjust if the layout file is named differently
        return new ViewHolder(view);
    }

    // 🧩 Bind data from the model to the ViewHolder's views.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TopSellingModel product = productList.get(position);
        holder.name.setText(product.getName());
        holder.size.setText(product.getSize());
        holder.sold.setText(product.getSold());
        holder.stock.setText(product.getStock());
        holder.status.setText(product.getStatus());

        // ✅ Set text color based on stock status
        if ("LOW".equalsIgnoreCase(product.getStatus())) {
            holder.status.setTextColor(holder.status.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }
    }


    // 🔢 Return total number of products in the list.
    @Override
    public int getItemCount() {
        return productList.size();
    }
}
