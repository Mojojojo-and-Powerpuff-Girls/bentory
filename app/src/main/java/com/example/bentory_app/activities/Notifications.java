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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.bentory_app.R;
import com.example.bentory_app.model.NotificationModel;
import com.example.bentory_app.repository.NotificationRepository;
import com.example.bentory_app.repository.ProductRepository;
import com.example.bentory_app.subcomponents.NotificationAdapter;
import java.util.ArrayList;
import java.util.Calendar;
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
    private ProductRepository productRepository;
    private LinearLayout emptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
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
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        notificationRepository = new NotificationRepository();
        productRepository = new ProductRepository();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setProgressViewOffset(true, 0, 200);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshNotifications();
        });

        // Load notifications
        loadNotifications();
    }

    private void refreshNotifications() {
        // Show refresh animation
        swipeRefreshLayout.setRefreshing(true);

        // Check for low stock products and refresh notifications
        notificationRepository.checkLowStockProducts();
        loadNotifications();
    }

    private void loadNotifications() {
        notificationRepository.getNotifications(new NotificationRepository.OnNotificationsLoadedListener() {
            @Override
            public void onNotificationsLoaded(List<NotificationModel> loadedNotifications) {
                runOnUiThread(() -> {
                    // Filter for LOW_STOCK and today only, then check product quantity
                    List<NotificationModel> filtered = new ArrayList<>();
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    long startOfDay = calendar.getTimeInMillis();
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    long endOfDay = calendar.getTimeInMillis();

                    // For async product fetches
                    List<NotificationModel> toCheck = new ArrayList<>();
                    for (NotificationModel n : loadedNotifications) {
                        if ("LOW_STOCK".equals(n.getType()) && n.getTimestamp() >= startOfDay
                                && n.getTimestamp() <= endOfDay) {
                            toCheck.add(n);
                        }
                    }
                    if (toCheck.isEmpty()) {
                        notifications.clear();
                        adapter.updateNotifications(notifications);
                        emptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }
                    // Counter for async completion
                    final int[] remaining = { toCheck.size() };
                    for (NotificationModel n : toCheck) {
                        productRepository.getProductById(n.getProductId(), new ProductRepository.ProductCallback() {
                            @Override
                            public void onProductFound(com.example.bentory_app.model.ProductModel product) {
                                if (product.getQuantity() <= NotificationRepository.LOW_STOCK_THRESHOLD) {
                                    filtered.add(n);
                                }
                                if (--remaining[0] == 0) {
                                    notifications.clear();
                                    notifications.addAll(filtered);
                                    adapter.updateNotifications(notifications);
                                    if (notifications.isEmpty()) {
                                        emptyState.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                    } else {
                                        emptyState.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }

                            @Override
                            public void onProductNotFound() {
                                if (--remaining[0] == 0) {
                                    notifications.clear();
                                    notifications.addAll(filtered);
                                    adapter.updateNotifications(notifications);
                                    if (notifications.isEmpty()) {
                                        emptyState.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                    } else {
                                        emptyState.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (--remaining[0] == 0) {
                                    notifications.clear();
                                    notifications.addAll(filtered);
                                    adapter.updateNotifications(notifications);
                                    if (notifications.isEmpty()) {
                                        emptyState.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                    } else {
                                        emptyState.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Handle error
                    swipeRefreshLayout.setRefreshing(false);
                });
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