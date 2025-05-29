package com.example.bentory_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.model.TopSellingModel;
import com.example.bentory_app.subcomponents.MenuAdapter;
import com.example.bentory_app.subcomponents.TopSellingAdapter;

import java.util.ArrayList;
import java.util.List;

public class Statistics extends AppCompatActivity {

    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backBtn = findViewById(R.id.back_btn);

        // SETUP RecyclerView
        RecyclerView recyclerViewTop = findViewById(R.id.recyclerViewTopStatistics);
        List<StatsModel> itemList1 = new ArrayList<>();
        itemList1.add(new StatsModel("Total Sales", "Month of April", "PHP 55,456.90"));
        itemList1.add(new StatsModel("Total Expenses", "Month of April", "PHP 10,456.00"));
        itemList1.add(new StatsModel("Net Profit", "Month of April", "PHP 45,000.90"));
        itemList1.add(new StatsModel("Projected Sales", "Month of May", "PHP 70,000.00"));

        MenuAdapter adapter1 = new MenuAdapter(itemList1);
        recyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTop.setAdapter(adapter1);

        // SETUP RecyclerView
        RecyclerView recyclerViewBottom = findViewById(R.id.recyclerViewBottomStatistics);
        List<TopSellingModel> itemList2 = new ArrayList<>();
        itemList2.add(new TopSellingModel("Coke", "Mismo", "10", "30", "OK"));
        itemList2.add(new TopSellingModel("Coke", "Mismo", "10", "30", "OK"));
        itemList2.add(new TopSellingModel("Coke", "Mismo", "10", "30", "OK"));
        itemList2.add(new TopSellingModel("Coke", "Mismo", "10", "30", "OK"));

        TopSellingAdapter adapter2 = new TopSellingAdapter(itemList2);
        recyclerViewBottom.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBottom.setAdapter(adapter2);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}