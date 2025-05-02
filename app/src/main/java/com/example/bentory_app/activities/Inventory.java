package com.example.bentory_app.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.subcomponents.InventoryAdapter;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class Inventory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductViewModel productViewModel;
    private InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.getItems().observe(this, new Observer<List<ProductModel>>() {
            @Override
            public void onChanged(List<ProductModel> itemList) {
                adapter = new InventoryAdapter(Inventory.this, itemList, item ->{
                    showBottomSheet(item);
                });
                recyclerView.setAdapter(adapter);
            }
        });

    }

    private void showBottomSheet(ProductModel product) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_product, null);
        bottomSheetView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                700 // height in pixels
        ));

        TextView name = bottomSheetView.findViewById(R.id.product_name);
        TextView detailsLabel = bottomSheetView.findViewById(R.id.product_details_label);
        TextView details = bottomSheetView.findViewById(R.id.product_details);

        name.setText(product.getName());

        String combinedDetailsLabel = "Category: " +  "\n" +
                "Quantity: " +  "\n" +
                "Cost Price: " +  "\n" +
                "Sale Price: " + "\n" +
                "Size: " +  "\n" +
                "Weight: " + "\n" +
                "Description: ";

        detailsLabel.setText(combinedDetailsLabel);

        String combinedDetails = product.getCategory() + "\n" +
                product.getQuantity() + "\n" +
                product.getCostPrice() + "\n" +
                product.getSalePrice() + "\n" +
                product.getSize() + "\n" +
                product.getWeight() + "\n" +
                product.getDescription();

        details.setText(combinedDetails);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

}