package com.example.bentory_app.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
// import androidx.appcompat.app.AppCompatActivity; // Extends BaseActivity
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// import androidx.appcompat.widget.Toolbar; // Handled by BaseActivity
// import androidx.appcompat.app.ActionBar; // Handled by BaseActivity

import com.example.bentory_app.R;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.model.TopSellingModel;
import com.example.bentory_app.subcomponents.MenuAdapter;
import com.example.bentory_app.subcomponents.TopSellingAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;



import java.util.ArrayList;
import java.util.List;

// ===============================
// Statistics Activity
//
// Purpose:
// - Displays key financial statistics for the app, such as sales, expenses, profit, etc.
// - Uses a RecyclerView to list summarized stats.
// - Utilizes a custom adapter (MenuAdapter) for clean item display.
// ===============================
public class Statistics extends BaseDrawerActivity { // Extends BaseDrawerActivity

    // UI Components
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        setupToolbar(R.id.my_toolbar, "Statistics", true); //// 'setupToolbar' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).
        setupDrawer(); //// 'setupDrawer' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).

        // ⬛ Bind Views
        backBtn = findViewById(R.id.back_btn);



        // SETUP RecyclerView
        RecyclerView recyclerViewTop = findViewById(R.id.recyclerViewTopStatistics);
        List<StatsModel> statsList = new ArrayList<>();
        MenuAdapter adapter1 = new MenuAdapter(statsList);
        recyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTop.setAdapter(adapter1);

        DatabaseReference statsRef = FirebaseDatabase.getInstance().getReference("selling_stats");
        DatabaseReference monthRef = statsRef.child("monthly_stats").child("month6");
        DatabaseReference weekRef = statsRef.child("weekly_sale").child("week23");

        monthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalSales = snapshot.child("total_sales").getValue(Double.class) != null ? snapshot.child("total_sales").getValue(Double.class) : 0.0;
                double profit = snapshot.child("monthly_profit").getValue(Double.class) != null ? snapshot.child("monthly_profit").getValue(Double.class) : 0.0;
                int productsSold = snapshot.child("products_sold").getValue(Integer.class) != null ? snapshot.child("products_sold").getValue(Integer.class) : 0;

                statsList.add(new StatsModel("Total Sales", "Month of June", "PHP " + totalSales));
                statsList.add(new StatsModel("Monthly Profit", "Month of June", "PHP " + profit));
                statsList.add(new StatsModel("Products Sold", "Month of June", String.valueOf(productsSold)));

                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        weekRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double weeklySale = snapshot.child("total_sale").getValue(Double.class) != null ? snapshot.child("total_sale").getValue(Double.class) : 0.0;
                statsList.add(0, new StatsModel("Weekly Sales", "Week 23", "PHP " + weeklySale));
                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });




        recyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTop.setAdapter(adapter1);

        // SETUP RecyclerView
        RecyclerView recyclerViewBottom = findViewById(R.id.recyclerViewBottomStatistics);
        List<TopSellingModel> topSellingList = new ArrayList<>();
        TopSellingAdapter adapter2 = new TopSellingAdapter(topSellingList);
        recyclerViewBottom.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBottom.setAdapter(adapter2);

        DatabaseReference topSellingRef = FirebaseDatabase.getInstance().getReference("selling_stats")
                .child("top_selling")
                .child("month6");

        topSellingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topSellingList.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    String name = itemSnap.child("name").getValue(String.class);
                    String size = itemSnap.child("size").getValue(String.class);
                    int sold = itemSnap.child("sold").getValue(Integer.class) != null ? itemSnap.child("sold").getValue(Integer.class) : 0;
                    int stock = itemSnap.child("stock").getValue(Integer.class) != null ? itemSnap.child("stock").getValue(Integer.class) : 0;

                    topSellingList.add(new TopSellingModel(name, size, String.valueOf(sold), String.valueOf(stock), "OK"));
                }
                adapter2.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}