package com.example.bentory_app.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.subcomponents.CartAdapter;
import com.example.bentory_app.subcomponents.MenuAdapter;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.bottom_sheet_cart);

        // SETUP RecyclerView
        RecyclerView recyclerViewTop = findViewById(R.id.recyclerViewCart);
        List<CartModel> itemList1 = new ArrayList<>();
        itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));
        itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));
        itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));
        itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));



        CartAdapter adapter1 = new CartAdapter(itemList1);
        recyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTop.setAdapter(adapter1);

    }
}
