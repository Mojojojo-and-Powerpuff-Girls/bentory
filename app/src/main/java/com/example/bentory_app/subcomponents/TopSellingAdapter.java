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

    private List<TopSellingModel> productList;

    public TopSellingAdapter(List<TopSellingModel> productList) {
        this.productList = productList;
    }

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

    @Override
    public TopSellingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_top_selling_layout, parent, false); // Adjust if the layout file is named differently
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TopSellingModel product = productList.get(position);
        holder.name.setText(product.getName());
        holder.size.setText(product.getSize());
        holder.sold.setText(product.getSold());
        holder.stock.setText(product.getStock());
        holder.status.setText(product.getStatus());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
