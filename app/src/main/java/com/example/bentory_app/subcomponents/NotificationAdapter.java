package com.example.bentory_app.subcomponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bentory_app.R;
import com.example.bentory_app.model.NotificationModel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationModel> notifications;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public NotificationAdapter(List<NotificationModel> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item_layout, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);

        holder.titleView.setText(notification.getTitle());
        holder.messageView.setText(notification.getMessage());

        if (notification.getTimestamp() != null) {
            holder.timeView.setText(dateFormat.format(notification.getDate()));
        }

        // Set text color based on notification type
        int textColor;
        switch (notification.getType()) {
            case "LOW_STOCK":
                textColor = holder.itemView.getContext().getResources().getColor(R.color.low_stock_red);
                break;
            case "SALES_UPDATE":
                textColor = holder.itemView.getContext().getResources().getColor(R.color.sales_green);
                break;
            default:
                textColor = holder.itemView.getContext().getResources().getColor(R.color.default_blue);
                break;
        }
        holder.titleView.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<NotificationModel> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView messageView;
        TextView timeView;

        NotificationViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.notification_title);
            messageView = itemView.findViewById(R.id.notification_message);
            timeView = itemView.findViewById(R.id.notification_time);
        }
    }
}