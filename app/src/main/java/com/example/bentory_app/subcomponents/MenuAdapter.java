package com.example.bentory_app.subcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.StatsModel;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    // State
    private List<StatsModel> itemList;

    // 🧱 Constructor to set data list.
    public MenuAdapter(List<StatsModel> itemList) {
        this.itemList = itemList;
    }

    // 🧊 ViewHolder inner class to cache view references.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewDescription;
        public TextView textViewNumFigures;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.itemTitle);              // Title
            textViewDescription = view.findViewById(R.id.itemDescription);  // Description
            textViewNumFigures = view.findViewById(R.id.itemNumFigures);    // Statistic value
        }
    }

    // 🏗️ Inflate item layout and create ViewHolder.
    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(view);
    }

    // 🎨 Bind data from StatsModel to each item view.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StatsModel item = itemList.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewDescription.setText(item.getDescription());
        holder.textViewNumFigures.setText(item.getNumFigures());
    }

    // 🔢 Return the total number of items in the list.
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
