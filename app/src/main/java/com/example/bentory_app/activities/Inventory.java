package com.example.bentory_app.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Set;

public class Inventory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductViewModel productViewModel;
    private InventoryAdapter adapter;
    private ImageButton dltButton;

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

        // Initialize Views
        dltButton = findViewById(R.id.deleteBtn);
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

        // 1. Set click listener for the delete mode button (trash icon)
        dltButton.setOnClickListener(v -> {
            // Step 1: Toggle the current state of delete mode
            boolean isDeleteModeActive = !adapter.getDeleteMode(); // true if we're switching into delete mode

            if (isDeleteModeActive) {
                // Step 2: Entering delete mode
                adapter.setDeleteMode(true); // enable delete mode in the InventoryAdapter
                dltButton.setImageResource(R.drawable.add_item_save_button); // change the icon !!!PAPALITAN PA 'TO NG ICON!
            }
            else {
                // Step 3: Already in delete mode and user clicked to confirm or exit
                // Step 4: Get the list of selected items to delete (based on checkboxes)
                Set<String> selectedItems = adapter.getSelectedItems();

                if (!selectedItems.isEmpty()) {
                    // Step 5: If there are selected items, show a confirmation dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Items")
                            .setMessage("Are you sure you want to delete the selected items?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                // Step 6: If user confirms, call the ViewModel to delete selected items
                                // Backend logic: ViewModel calls repository -> repository deletes from database
                                productViewModel.deleteSelectedProducts(selectedItems);

                                // Step 7: After deletion, exit delete mode and reset button icon
                                adapter.setDeleteMode(false);
                                dltButton.setImageResource(R.drawable.scanner); // !!!PAPALITAN PA 'TO NG ICON!
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                // Step 8: If user cancels, exit delete mode without deleting
                                adapter.setDeleteMode(false);
                                dltButton.setImageResource(R.drawable.scanner); // !!!PAPALITAN PA 'TO NG ICON!
                            })
                            .show();
                }
                else {
                    // Step 9: If no items selected, just exit delete mode
                    adapter.setDeleteMode(false);
                    dltButton.setImageResource(R.drawable.scanner); // !!PAPALITAN PA 'TO NG ICON! [PATI XML]
                }
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
                product.getCost_Price() + "\n" +
                product.getSale_Price() + "\n" +
                product.getSize() + "\n" +
                product.getWeight() + "\n" +
                product.getDescription();

        details.setText(combinedDetails);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }



}