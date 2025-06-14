package com.example.bentory_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bentory_app.R;
import com.example.bentory_app.model.NotificationModel;
import com.example.bentory_app.repository.NotificationRepository;
import com.example.bentory_app.subcomponents.NotificationAdapter;
import java.util.ArrayList;
import java.util.List;

// ===============================
// Notifications Activity
//
// Purpose:
// - Displays notifications for the user.
// - Uses BaseDrawer Activity to enable toolbar and navigation drawer features.
// - Ensures proper window insets for edge-to-edge layout.
// ===============================
public class Notifications extends BaseDrawerActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationRepository notificationRepository;
    private LinearLayout emptyState;
    private List<NotificationModel> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar(R.id.my_toolbar, "Notifications", true); //// 'setupToolbar' contains a method (found at
                                                              //// 'BaseDrawerActivity' in 'activities' directory).
        setupDrawer(); //// 'setupDrawer' contains a method (found at 'BaseDrawerActivity' in
                       //// 'activities' directory).

        // Initialize components
        recyclerView = findViewById(R.id.notifications_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        notificationRepository = new NotificationRepository();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        // Load notifications
        loadNotifications();
    }

    private void loadNotifications() {
        notificationRepository.getNotifications(new NotificationRepository.OnNotificationsLoadedListener() {
            @Override
            public void onNotificationsLoaded(List<NotificationModel> loadedNotifications) {
                notifications.clear();
                notifications.addAll(loadedNotifications);
                adapter.updateNotifications(notifications);

                // Show/hide empty state
                if (notifications.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for low stock products and refresh notifications
        notificationRepository.checkLowStockProducts();
        loadNotifications();
    }
}