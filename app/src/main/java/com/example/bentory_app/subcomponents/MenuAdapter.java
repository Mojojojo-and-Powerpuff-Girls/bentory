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

    private List<StatsModel> itemList;

    public MenuAdapter(List<StatsModel> itemList) {
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewDescription;
        public TextView textViewNumFigures;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.itemTitle);
            textViewDescription = view.findViewById(R.id.itemDescription);
            textViewNumFigures = view.findViewById(R.id.itemNumFigures);
        }
    }

    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StatsModel item = itemList.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewDescription.setText(item.getDescription());
        holder.textViewNumFigures.setText(item.getNumFigures());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
