package com.example.bentory_app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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

public class SellProduct extends BaseDrawerActivity {

    private ToneGenerator toneGen;
    private Vibrator vibrator;
    private DecoratedBarcodeView barcodeView;
    private View targetOverlay, touchBlock;
    private boolean isScannerActive = false;
    private ImageButton scanBtn, filterBtn, searchScanBtn, backBtn;
    private EditText manualCode, searchEditText;
    private RecyclerView recyclerViewSelling;
    private SellingViewModel sellingViewModel;
    private CartViewModel cartViewModel;
    private SellingProductAdapter sellingAdapter;
    private ProductViewModel productViewModel;
    private List<ProductModel> fullProductList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sell_product);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup toolbar using BaseActivity method
        // For SellProduct, we likely want the burger menu, so showBurgerMenu is true
        setupToolbar(R.id.my_toolbar, "Selling Window", true);

        // Setup drawer functionality
        setupDrawer();

        // Initialize ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class); // !!!!!!!!!!!!!!!!!!!!
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sellingViewModel = new ViewModelProvider(this).get(SellingViewModel.class);

        // FindByViewById
        searchScanBtn = findViewById(R.id.searchScannerBtn);
        filterBtn = findViewById(R.id.filterBtn); // filter button
        searchEditText = findViewById(R.id.searchView); // search view
        targetOverlay = findViewById(R.id.targetOverlay); // barcode's target size
        touchBlock = findViewById(R.id.touchBlocker);
        barcodeView = findViewById(R.id.barcodeScanner); // setup barcode
        scanBtn = findViewById(R.id.scannerBtn); // setup barcode
        manualCode = findViewById(R.id.sellingCode); // setup barcode
        recyclerViewSelling = findViewById(R.id.recyclerViewSelling); // Setup RecyclerView for selling products
        recyclerViewSelling.setLayoutManager(new LinearLayoutManager(this));
        backBtn = findViewById(R.id.back_btn);

        // Search barcode
        searchScanBtn.setOnClickListener(v -> {
            scanBarcodeForProductSearch();
        });

        // Filter A-Z Z-A
        filterBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(SellProduct.this, filterBtn);
            popupMenu.getMenuInflater().inflate(R.menu.menu_filter, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (fullProductList == null) return false;
                List<ProductModel> sortedList = new ArrayList<>(fullProductList);

                if (item.getItemId() == R.id.menu_az) {
                    Collections.sort(sortedList, Comparator.comparing(ProductModel::getName));
                } else if (item.getItemId() == R.id.menu_za) {
                    Collections.sort(sortedList, Comparator.comparing(ProductModel::getName).reversed());
                }

                sellingAdapter.setProductList(sortedList);
                return true;
            });
            popupMenu.show();
        });

        // Search button
        productViewModel.getItems().observe(this, itemList -> {
            fullProductList = new ArrayList<>(itemList); // Save full list for filtering
            sellingAdapter.setProductList(itemList); // Set initial full list
        });

        // Search listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Setup adapter and handle Add to Cart logic
        sellingAdapter = new SellingProductAdapter(product -> {

            // Reduce product quantity by 1 when buttons are clicked
            int currentStock = product.getQuantity();
            // Checks if stock is zero.
            if (currentStock <= 0) {
                Toast.makeText(SellProduct.this, "Out of stock!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Proceeds to add to cart
            CartModel cartItem = new CartModel(
                    product.getId(),
                    product.getName(),
                    product.getSize(),
                    1,
                    product.getSale_Price(),
                    product // pass the reference
            );
            cartViewModel.addToCart(cartItem);
            Toast.makeText(SellProduct.this, "Item added!", Toast.LENGTH_SHORT).show();
            // Reduce product stock
            product.setQuantity(currentStock - 1);
            // Optional: Notify UI of changes if needed
            sellingAdapter.notifyDataSetChanged();
        });

        recyclerViewSelling.setAdapter(sellingAdapter);

        // Observe product data from SellingViewModel
        sellingViewModel.getItems().observe(this, productModels -> {
            sellingAdapter.setProductList(productModels);
        });


        ImageButton cartButton = findViewById(R.id.pullout_btn);
        cartButton.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SellProduct.this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_cart, null, false);

            bottomSheetDialog.setContentView(bottomSheetView);

            ImageButton confirmBtn = bottomSheetView.findViewById(R.id.confirm_button);
            TextView totalPriceView = bottomSheetView.findViewById(R.id.total_price_text);
            double total = cartViewModel.getCartTotal();
            totalPriceView.setText(String.format("₱%.2f", total));

            confirmBtn.setOnClickListener(view -> {
                cartViewModel.confirmCart(() -> {
                    Toast.makeText(this, "Stock updated and cart cleared", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                });
            });

            // Pause barcode scanner when bottom sheet is shown
            bottomSheetDialog.setOnShowListener(dialog -> {
                barcodeView.pause();

                // Method 1: Force full height using display metrics
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = 2100;

                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
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

            // Resume scanner only if it was active before when bottom sheet is dismissed
            bottomSheetDialog.setOnDismissListener(dialog -> {
                if (isScannerActive)
                    barcodeView.resume();
            });

            RecyclerView recyclerViewTop = bottomSheetView.findViewById(R.id.recyclerViewCart);
            recyclerViewTop.setLayoutManager(new LinearLayoutManager(SellProduct.this));
            cartViewModel.getCartItems().observe(SellProduct.this, cartItems -> {
                CartAdapter adapter1 = new CartAdapter(cartItems, () -> sellingAdapter.notifyDataSetChanged());
                recyclerViewTop.setAdapter(adapter1);
            });

            bottomSheetDialog.show();
        });

        // Start continuous barcode scanning with the provided callback
        barcodeView.decodeContinuous(callback);

        // Toggle scanner visibility and state when scan button is clicked
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

        // Initialize sounds and vibration
        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 200);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Manual input of code
        manualCode.setOnEditorActionListener((v, actionId, event) -> {
            String enteredCode = manualCode.getText().toString().trim();

            if (!enteredCode.isEmpty()) {
                List<ProductModel> products = sellingViewModel.getItems().getValue();
                if (products == null)
                    return true;

                // Search for product by code
                ProductModel matched = null;
                for (ProductModel p : products) {
                    if (p.getBarcode().contains(enteredCode)) {
                        matched = p;
                        break;
                    }
                }

                if (matched != null) {
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
                    matched.setQuantity(matched.getQuantity() - 1);
                    manualCode.setText(""); // clear input
                    sellingAdapter.notifyDataSetChanged();

                } else {
                    // Barcode not found - prompt user to link it
                    new AlertDialog.Builder(SellProduct.this)
                            .setTitle("Unknown Barcode")
                            .setMessage("This barcode is not associated with any product. \nWould you like to link it to an existing one?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Intent intent = new Intent(SellProduct.this, Inventory.class);
                                intent.putExtra("scannedBarcode", enteredCode); // pass the barcode
                                startActivity(intent);
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
            return true;
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Filter products method
    private void filterProducts(String query) {
        if (fullProductList == null) return;

        List<ProductModel> filtered = new ArrayList<>();
        for (ProductModel product : fullProductList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getSize().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(product);
            }
        }

        sellingAdapter.setProductList(filtered);
    }


    // Handle scanned barcode
    private void handleScannedBarcode(String scannedBarcode) {
        ProductRepository repo = new ProductRepository();
        repo.getProductByBarcode(scannedBarcode, new ProductRepository.ProductCallback() {
            @Override
            public void onProductFound(ProductModel product) {
                int currentStock = product.getQuantity();
                if (currentStock <= 0) {
                    Toast.makeText(SellProduct.this, "Out of stock!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add to cart
                CartModel cartItem = new CartModel(
                        product.getId(),
                        product.getName(),
                        product.getSize(),
                        1,
                        product.getSale_Price(),
                        product);
                cartViewModel.addToCart(cartItem);

                product.setQuantity(currentStock - 1);
                sellingAdapter.notifyDataSetChanged();

            }

            @Override
            public void onProductNotFound() {
                toneGen.startTone(ToneGenerator.TONE_SUP_ERROR); // distinct tone for not found
                runOnUiThread(() -> {
                    new AlertDialog.Builder(SellProduct.this)
                            .setTitle("Unknown Barcode")
                            .setMessage("This barcode is not associated with any product. \nWould you like to link it to an existing one?")
                            .setPositiveButton("Yes", (dialog,which) -> {
                                Intent intent = new Intent(SellProduct.this, Inventory.class);
                                intent.putExtra("scannedBarcode", scannedBarcode);
                                startActivity(intent);
                            })
                            .setNegativeButton("No", null)
                            .show();
                });

            }

            @Override
            public void onError(String error) {
                Toast.makeText(SellProduct.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle Barcode scans in real time.
    private final BarcodeCallback callback = new BarcodeCallback() {
        private long lastScanTime = 0;
        private static final long SCAN_COOLDOWN_MS = 1000; // 1 second cooldown

        @Override
        public void barcodeResult(BarcodeResult result) {
            String scannedCode = result.getText();
            long currentTime = System.currentTimeMillis();

            // Prevent duplicate scan within cooldown time
            if (currentTime - lastScanTime < SCAN_COOLDOWN_MS) {
                return;
            }
            lastScanTime = currentTime;

            // Lookup product from firebase
            ProductModel matched = null;
            for (ProductModel p : sellingViewModel.getItems().getValue()) {
                if (p.getBarcode() != null && p.getBarcode().contains(scannedCode)) {
                    matched = p;
                    break;
                }
            }

            if (matched != null) {
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
                    return; // STOP execution
                }

                // Add to cart
                CartModel cartItem = new CartModel(
                        matched.getId(), matched.getName(), matched.getSize(), 1,
                        matched.getSale_Price(), matched);
                cartViewModel.addToCart(cartItem);
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

                sellingAdapter.notifyDataSetChanged();

            } else {
                toneGen.startTone(ToneGenerator.TONE_SUP_ERROR); // distinct tone for not found
                // Stop the tone after a short delay (e.g., 500ms)
                new android.os.Handler().postDelayed(() -> {
                    toneGen.stopTone();
                }, 500);
                runOnUiThread(() -> {
                    new AlertDialog.Builder(SellProduct.this)
                            .setTitle("Unknown Barcode")
                            .setMessage("This barcode is not associated with any product. \nWould you like to link it to an existing one?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Intent intent = new Intent(SellProduct.this, Inventory.class);
                                intent.putExtra("scannedBarcode", scannedCode); // pass the barcode
                                startActivity(intent);
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }
        }
    };

    // BALIKAN TO!!!!!!!!!!!!!!!!!!!!!!!!
    private void showProductSelectionDialog(String newBarcode) {
        List<ProductModel> products = sellingViewModel.getItems().getValue();
        if (products == null || products.isEmpty()) return;

        String[] productNames = new String[products.size()];
        for (int i = 0; i < products.size(); i++) {
            productNames[i] = products.get(i).getName() + " - " + products.get(i).getSize();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Product to Associate")
                .setItems(productNames, (dialog, which) -> {
                    ProductModel selected = products.get(which);

                    // If barcode already exists, show toast immediately
                    if (selected.getBarcode() != null && selected.getBarcode().contains(newBarcode)) {
                        Toast.makeText(this, "Barcode already exists for this product.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Confirmation Dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Confirm Association")
                            .setMessage("Are you sure you want to add this barcode to " + selected.getName() + "?")
                            .setPositiveButton("Yes", (confirmDialog, confirmWhich) -> {
                                // Add barcode
                                selected.getBarcode().add(newBarcode);
                                sellingViewModel.updateProduct(selected);
                                Toast.makeText(this, "Barcode linked to " + selected.getName(), Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // Scans code for product search method
    private void scanBarcodeForProductSearch() {
        barcodeView.setVisibility(View.VISIBLE);
        targetOverlay.setVisibility(View.VISIBLE);
        touchBlock.setVisibility(View.VISIBLE);
        isScannerActive = true;

        barcodeView.resume();

        barcodeView.decodeSingle(new BarcodeCallback() {
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
                        List<ProductModel> filteredList = new ArrayList<>();
                        filteredList.add(matched);
                        sellingAdapter.setProductList(filteredList);

                        // Cleanup scanner
                        barcodeView.pause();
                        barcodeView.setVisibility(View.GONE);
                        targetOverlay.setVisibility(View.GONE);
                        touchBlock.setVisibility(View.GONE);
                        isScannerActive = false;

                    } else {
                        Toast.makeText(SellProduct.this, "Product not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isScannerActive)
            barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }


}