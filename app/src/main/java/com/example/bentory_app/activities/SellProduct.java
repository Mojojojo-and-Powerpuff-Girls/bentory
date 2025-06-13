package com.example.bentory_app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;
import com.example.bentory_app.subcomponents.CartAdapter;
import com.example.bentory_app.subcomponents.SellingProductAdapter;
import com.example.bentory_app.viewmodel.CartViewModel;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.example.bentory_app.viewmodel.SellingViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// ===============================
// SellProduct Activity
//
// Purpose:
// - Allows users to sell products by scanning barcodes or selecting from the inventory.
// - Handles updating stock quantities after a product sale.
// - Connects with firebase to update product information in real-time.
// - Includes UI for entering quantity, confirming the sale, and viewing cart details.
// ===============================
public class SellProduct extends BaseDrawerActivity {

    // UI Components
    private RecyclerView recyclerViewSelling, recyclerViewTop;
    private ImageButton scanBtn, filterBtn, searchScanBtn, backBtn, cartButton, confirmBtn;
    private Button btnYes, btnNo;
    private EditText manualCode, searchEditText;
    private DecoratedBarcodeView barcodeView;
    private View targetOverlay, touchBlock, bottomSheetView, dialogView;
    private ToneGenerator toneGen;
    private Vibrator vibrator;
    private TextView totalPriceView;
    private FrameLayout bottomSheet;
    private AlertDialog alertDialog;

    // State
    private boolean isScannerActive = false;
    private double total;
    private int screenHeight;
    private String enteredCode;
    private List<ProductModel> fullProductList;

    // ViewModel
    private CartViewModel cartViewModel;
    private SellingViewModel sellingViewModel;
    private ProductViewModel productViewModel;

    // Adapter
    private SellingProductAdapter sellingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sell_product);

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        setupToolbar(R.id.my_toolbar, "Selling Window", true); //// 'setupToolbar' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).
        setupDrawer(); //// 'setupDrawer' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).

        // ⬛ Initialize ViewModel
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sellingViewModel = new ViewModelProvider(this).get(SellingViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 200); // Initialize sounds and vibration
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // ⬛ Bind Views
        searchScanBtn = findViewById(R.id.searchScannerBtn);
        filterBtn = findViewById(R.id.filterBtn);
        searchEditText = findViewById(R.id.searchView);
        targetOverlay = findViewById(R.id.targetOverlay);
        touchBlock = findViewById(R.id.touchBlocker);
        barcodeView = findViewById(R.id.barcodeScanner);
        scanBtn = findViewById(R.id.scannerBtn);
        manualCode = findViewById(R.id.sellingCode);
        recyclerViewSelling = findViewById(R.id.recyclerViewSelling);
        backBtn = findViewById(R.id.back_btn);
        cartButton = findViewById(R.id.pullout_btn);

        // 📦 Setup RecyclerView layout manager for the selling products list.
        recyclerViewSelling.setLayoutManager(new LinearLayoutManager(this));

        // ===============================
        // 🔍 Search Input: Filter product list as user types in search field. (FEATURE)
        // ===============================
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString()); //// 'filterProducts' contains a method (found at the bottom of the code).
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // ===============================
        // 🔍 Search barcode button click: start barcode scanning for product search. (FEATURE)
        // ===============================
        searchScanBtn.setOnClickListener(v -> {
            scanBarcodeForProductSearch(); //// 'scanBarcodeForProductSearch' contains a method (found at the bottom of the code).
        });


        // ===============================
        // 🔽 Filter Button Setup: Show sorting options for the inventory list [A-Z / Z-A]. (FEATURE)
        // ===============================
        filterBtn.setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(wrapper, filterBtn);
            popupMenu.getMenuInflater().inflate(R.menu.menu_filter, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (fullProductList == null)
                    return false;
                List<ProductModel> sortedList = new ArrayList<>(fullProductList);

                if (item.getItemId() == R.id.menu_az) {
                    Collections.sort(sortedList, Comparator.comparing(ProductModel::getName));
                } else if (item.getItemId() == R.id.menu_za) {
                    Collections.sort(sortedList, Comparator.comparing(ProductModel::getName).reversed());
                }

                sellingAdapter.setProductList(sortedList); //// 'setProductList' contains a method (found at 'SellingProductAdapter' in 'subcomponents' directory).
                return true;
            });
            popupMenu.show();
        });


        // 🔁 Observe data from ViewModel : Keeps product data up to date and refreshes UI.
        productViewModel.getItems().observe(this, itemList -> {
            fullProductList = new ArrayList<>(itemList); // keep original list for filtering.
            sellingAdapter.setProductList(itemList);     // updates the sellingAdapter with new or updated list of products.
        });

        // ===============================
        // ➕🛒 Add to Cart Logic: Initialize sellingAdapter with Add to Cart Button logic ['+' button]. (FEATURE)
        // ===============================
        sellingAdapter = new SellingProductAdapter(product -> {
            int currentStock = product.getQuantity();

            // Checks if stock is zero.
            if (currentStock <= 0) {
                Toast.makeText(SellProduct.this, "Out of stock!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add product to cart with quantity 1.
            CartModel cartItem = new CartModel(
                    product.getId(),
                    product.getName(),
                    product.getSize(),
                    1,
                    product.getSale_Price(),
                    product // pass the reference
            );
            cartViewModel.addToCart(cartItem); //// 'addToCart' contains a method (found at 'CartViewModel' in 'viewmodel' directory).
            Toast.makeText(SellProduct.this, "Item added!", Toast.LENGTH_SHORT).show();

            // Reduce product stock after add to cart.
            product.setQuantity(currentStock - 1);

            // Optional: Notify UI of changes if needed.
            sellingAdapter.notifyDataSetChanged();
        });
        recyclerViewSelling.setAdapter(sellingAdapter);


        // 🔁 Observe data from ViewModel : Keeps product data up to date and refreshes UI.
        sellingViewModel.getItems().observe(this, productModels -> {
            sellingAdapter.setProductList(productModels);
        });

        // ===============================
        // ✅🛒 Confirm button and Cart bottom sheet: confirm button logic and show cart bottom sheet dialog. (FEATURE)
        // ===============================
        cartButton.setOnClickListener(v -> {
            // Initialize and set up bottom sheet view.
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SellProduct.this);
            bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_cart, null, false);
            bottomSheetDialog.setContentView(bottomSheetView);

            // Display total price and bind confirm button.
            confirmBtn = bottomSheetView.findViewById(R.id.confirm_button);
            totalPriceView = bottomSheetView.findViewById(R.id.total_price_text);
            total = cartViewModel.getCartTotal(); //// 'getCartTotal' contains a method (found at 'CartViewModel' in 'viewmodel' directory).
            totalPriceView.setText(String.format("₱%.2f", total));

            // On confirm: update stocks adn clear cart.
            confirmBtn.setOnClickListener(view -> {
                //// 'confirmCart' contains a method (found at 'CartViewModel' in 'viewmodel' directory).
                cartViewModel.confirmCart(() -> {
                    Toast.makeText(this, "Stock updated and Cart cleared", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                });
            });

            // Pause barcode scanner when bottom sheet [cart] is shown.
            bottomSheetDialog.setOnShowListener(dialog -> {
                barcodeView.pause();

                // Force full height using display metrics.
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                screenHeight = 2100;

                bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                    ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                    layoutParams.height = screenHeight;
                    bottomSheet.setLayoutParams(layoutParams);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                    behavior.setPeekHeight(screenHeight);
                }
            });

            // Resume scanner when bottom sheet [cart] is dismissed. (If it was active)
            bottomSheetDialog.setOnDismissListener(dialog -> {
                if (isScannerActive)
                    barcodeView.resume();
            });

            // Display cart items in RecyclerView inside bottom sheet [cart].
            recyclerViewTop = bottomSheetView.findViewById(R.id.recyclerViewCart);
            recyclerViewTop.setLayoutManager(new LinearLayoutManager(SellProduct.this));

            //// 'getCartItems' contains a method (found at 'CartViewModel' in 'viewmodel' directory).
            cartViewModel.getCartItems().observe(SellProduct.this, cartItems -> {
                CartAdapter adapter1 = new CartAdapter(cartItems, () -> sellingAdapter.notifyDataSetChanged());
                recyclerViewTop.setAdapter(adapter1);
            });

            bottomSheetDialog.show();
        });


        // ===============================
        // 📷 Barcode Scanner Setup: Barcode scanner visibility and state. (FEATURE)
        // ===============================
        barcodeView.decodeContinuous(callback); //// 'callback' contains a method (found at the bottom of the code).
        scanBtn.setOnClickListener(v -> {
            if (isScannerActive) {
                barcodeView.setVisibility(View.GONE);
                targetOverlay.setVisibility(View.GONE);
                touchBlock.setVisibility(View.GONE);
                barcodeView.pause();
            } else {
                barcodeView.setVisibility(View.VISIBLE);
                targetOverlay.setVisibility(View.VISIBLE);
                touchBlock.setVisibility(View.VISIBLE);
                barcodeView.resume();
            }
            isScannerActive = !isScannerActive; // Update scanner state flag
        });


        // ===============================
        // 🖊️ Manual Barcode Entry: Handle manual code input via keyboard done/enter action [also includes the linking of new barcode]. (FEATURE)
        // ===============================
        manualCode.setOnEditorActionListener((v, actionId, event) -> {
            enteredCode = manualCode.getText().toString().trim();

            if (!enteredCode.isEmpty()) {
                List<ProductModel> products = sellingViewModel.getItems().getValue();
                if (products == null)
                    return true;

                // Search for a product that contains the entered code in any of its barcodes.
                ProductModel matched = null;
                for (ProductModel p : products) {
                    if (p.getBarcode() != null && p.getBarcode().contains(enteredCode)) {
                        matched = p;
                        break;
                    }
                }

                if (matched != null) {
                    // Check stock before adding to cart.
                    if (matched.getQuantity() <= 0) {
                        manualCode.setError("Out of stock");
                        return true;
                    }

                    // Found a product, Add to cart
                    CartModel cartItem = new CartModel(
                            matched.getId(),
                            matched.getName(),
                            matched.getSize(),
                            1,
                            matched.getSale_Price(),
                            matched);

                    cartViewModel.addToCart(cartItem);
                    Toast.makeText(SellProduct.this, "Item added!", Toast.LENGTH_SHORT).show();

                    // Decrease stock and refresh UI.
                    matched.setQuantity(matched.getQuantity() - 1);
                    manualCode.setText(""); // clear input field.
                    sellingAdapter.notifyDataSetChanged();

                } else {
                    // Barcode not found, ask user if they want to link the barcode.
                    dialogView = LayoutInflater.from(SellProduct.this).inflate(R.layout.custom_alert_dialog, null);
                    alertDialog = new AlertDialog.Builder(SellProduct.this)
                            .setView(dialogView)
                            .create();

                    // Handle buttons inside the custom view.
                    btnYes = dialogView.findViewById(R.id.button_yes);
                    btnNo = dialogView.findViewById(R.id.button_no);

                    btnYes.setOnClickListener(v1 -> {
                        Intent intent = new Intent(SellProduct.this, Inventory.class);
                        intent.putExtra("scannedBarcode", enteredCode); // pass entered code to Inventory.
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "Select a product to link the barcode.", Toast.LENGTH_LONG).show();
                    });

                    btnNo.setOnClickListener(v2 -> {
                        alertDialog.dismiss();
                    });
                    alertDialog.show();
                }
            }
            return true;
        });


        // ===============================
        // 🔙 Back Button: Close Activity. (FEATURE)
        // ===============================
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeView.getVisibility() == View.VISIBLE) {
                    // Hide scanner and show form
                    barcodeView.setVisibility(View.GONE);
                    targetOverlay.setVisibility(View.GONE);
                    touchBlock.setVisibility(View.GONE);
                    barcodeView.pause(); // stop scanning
                } else {
                    Intent intent = new Intent(SellProduct.this, LandingPage.class);
                    startActivity(intent);
                    finish(); // optional, closes current activity
                }

            }
        });
    }


    // ===============================
    //             METHODS
    // ===============================

    // 🔎 'filterProducts' : Filters the product list based on user query from the search input. (METHODS)
    private void filterProducts(String query) {
        if (fullProductList == null)
            return;

        List<ProductModel> filtered = new ArrayList<>();
        for (ProductModel product : fullProductList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getSize().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(product);
            }
        }
        sellingAdapter.setProductList(filtered);
    }


    // 📷 'callback' : This callback handles barcode scanning events. (METHODS)
    private final BarcodeCallback callback = new BarcodeCallback() {
        private long lastScanTime = 0;
        private static final long SCAN_COOLDOWN_MS = 1000; // 1 second cooldown between scans.

        @Override
        public void barcodeResult(BarcodeResult result) {
            String scannedCode = result.getText();
            long currentTime = System.currentTimeMillis();

            // Prevent duplicate scan within cooldown time
            if (currentTime - lastScanTime < SCAN_COOLDOWN_MS) {
                return;
            }
            lastScanTime = currentTime;

            // Lookup product by barcode from ViewModel's current items (Firebase).
            ProductModel matched = null;
            for (ProductModel p : sellingViewModel.getItems().getValue()) {
                if (p.getBarcode() != null && p.getBarcode().contains(scannedCode)) {
                    matched = p;
                    break;
                }
            }

            if (matched != null) {
                // Out of stock checker.
                if (matched.getQuantity() <= 0) {
                    toneGen.startTone(ToneGenerator.TONE_PROP_NACK); // Out-of-stock sound
                    if (vibrator != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(150);
                        }
                    }
                    Toast.makeText(SellProduct.this, "Out of stock!", Toast.LENGTH_SHORT).show();
                    return;                                         // STOP execution if no stock.
                }

                // Add to cart with quantity 1.
                CartModel cartItem = new CartModel(
                        matched.getId(), matched.getName(), matched.getSize(), 1,
                        matched.getSale_Price(), matched);
                cartViewModel.addToCart(cartItem);

                // Decrement product quantity in stock by 1.
                matched.setQuantity(matched.getQuantity() - 1);

                // Add sound and vibration feedback
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP);
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // For API 26 and above
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        // For below API 26
                        vibrator.vibrate(100); // Deprecated but works in older versions
                    }
                }

                // Notify adapter to refresh UI.
                sellingAdapter.notifyDataSetChanged();

            } else {
                // distinct tone for not found
                toneGen.startTone(ToneGenerator.TONE_SUP_ERROR);

                // Stop the tone after a short delay (e.g., 500ms)
                new android.os.Handler().postDelayed(() -> {
                    toneGen.stopTone();
                }, 500);

                // Show dialog to link new barcode to existing product.
                runOnUiThread(() -> {
                    // Barcode not found, ask user if they want to link the barcode.
                    dialogView = LayoutInflater.from(SellProduct.this).inflate(R.layout.custom_alert_dialog, null);
                    alertDialog = new AlertDialog.Builder(SellProduct.this)
                            .setView(dialogView)
                            .create();

                    // Handle buttons inside the custom view.
                    btnYes = dialogView.findViewById(R.id.button_yes);
                    btnNo = dialogView.findViewById(R.id.button_no);

                    btnYes.setOnClickListener(v3 -> {
                        Intent intent = new Intent(SellProduct.this, Inventory.class);
                        intent.putExtra("scannedBarcode", scannedCode); // pass entered code to Inventory.
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "Select a product to link the barcode.", Toast.LENGTH_LONG).show();
                    });

                    btnNo.setOnClickListener(v4 -> {
                        alertDialog.dismiss();
                    });
                    alertDialog.show();
                });
            }
        }
    };


    // 📲 'scanBarcodeForProductSearch' : Scans barcode to filter products for search. (METHODS)
    private void scanBarcodeForProductSearch() {
        // Make barcode scanner visible.
        barcodeView.setVisibility(View.VISIBLE);
        targetOverlay.setVisibility(View.VISIBLE);
        touchBlock.setVisibility(View.VISIBLE);
        isScannerActive = true;

        barcodeView.resume();

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String searchScannedCode = result.getText();
                List<ProductModel> products = sellingViewModel.getItems().getValue();

                if (products != null) {
                    ProductModel matched = null;
                    for (ProductModel p : products) {
                        if (p.getBarcode() != null && p.getBarcode().contains(searchScannedCode)) {
                            matched = p;
                            break;
                        }
                    }

                    if (matched != null) {
                        // Show only the matched product in the adapter (filtered).
                        List<ProductModel> filteredList = new ArrayList<>();
                        filteredList.add(matched);
                        sellingAdapter.setProductList(filteredList);

                        // Cleanup scanner UI
                        barcodeView.pause();
                        barcodeView.setVisibility(View.GONE);
                        targetOverlay.setVisibility(View.GONE);
                        touchBlock.setVisibility(View.GONE);
                        isScannerActive = false;

                    } else {
                        barcodeView.pause();
                        isScannerActive = false;

                        // Barcode not found, ask user if they want to link the barcode.
                        dialogView = LayoutInflater.from(SellProduct.this).inflate(R.layout.custom_alert_dialog, null);
                        alertDialog = new AlertDialog.Builder(SellProduct.this)
                                .setView(dialogView)
                                .create();

                        // Handle buttons inside the custom view.
                        btnYes = dialogView.findViewById(R.id.button_yes);
                        btnNo = dialogView.findViewById(R.id.button_no);

                        btnYes.setOnClickListener(v3 -> {
                            Intent intent = new Intent(SellProduct.this, Inventory.class);
                            intent.putExtra("scannedBarcode", searchScannedCode); // pass entered code to Inventory.
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Select a product to link the barcode.", Toast.LENGTH_LONG).show();
                        });

                        btnNo.setOnClickListener(v4 -> {
                            barcodeView.resume();
                            alertDialog.dismiss();
                        });
                        alertDialog.show();
                    }
                }
            }
        });
    }

    // Resume Scanner (METHODS)
    @Override
    protected void onResume() {
        super.onResume();
        if (isScannerActive)
            barcodeView.resume();
    }

    // Pause Scanner (METHODS)
    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}