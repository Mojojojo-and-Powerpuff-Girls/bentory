package com.example.bentory_app.activities;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

import com.example.bentory_app.R;
import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.repository.ProductRepository;
import com.example.bentory_app.subcomponents.CartAdapter;
import com.example.bentory_app.subcomponents.InventoryAdapter;
import com.example.bentory_app.subcomponents.MenuAdapter;
import com.example.bentory_app.subcomponents.SellingProductAdapter;
import com.example.bentory_app.viewmodel.CartViewModel;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.example.bentory_app.viewmodel.SellingViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.List;

public class SellProduct extends AppCompatActivity {

    private ToneGenerator toneGen;
    private Vibrator vibrator;
    private DecoratedBarcodeView barcodeView;
    private boolean isScannerActive = false;
    private ImageButton scanBtn;
    private EditText manualCode;
    private RecyclerView recyclerViewSelling;
    private SellingViewModel sellingViewModel;
    private CartViewModel cartViewModel;
    private SellingProductAdapter sellingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sell_product);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // setup toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Set the title using the custom TextView in the toolbar
        TextView toolbarTitle = myToolbar.findViewById(R.id.textView);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Sell Product");
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(false);

            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Initialize ViewModels
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sellingViewModel = new ViewModelProvider(this).get(SellingViewModel.class);

        // FindByViewById
        barcodeView = findViewById(R.id.barcodeScanner); // setup barcode
        scanBtn = findViewById(R.id.scannerBtn); // setup barcode
        manualCode = findViewById(R.id.sellingCode); // setup barcode
        recyclerViewSelling = findViewById(R.id.recyclerViewSelling); // Setup RecyclerView for selling products
        recyclerViewSelling.setLayoutManager(new LinearLayoutManager(this));

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

        // Set up cart button click listener to show BottomSheet with cart items
        ImageButton cartButton = findViewById(R.id.pullout_btn);
        cartButton.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SellProduct.this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_cart, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            // Pause barcode scanner when bottom sheet is shown
            bottomSheetDialog.setOnShowListener(dialog -> barcodeView.pause());
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
                barcodeView.pause();
            } else {
                barcodeView.setVisibility(View.VISIBLE);
                barcodeView.resume();
            }
            isScannerActive = !isScannerActive; // Update scanner state flag
        });

        // Initialize sounds and vibration
        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
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
                for (ProductModel p : sellingViewModel.getItems().getValue()) {
                    if (p.getBarcode().equals(enteredCode)) {
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
                    manualCode.setError("Product not found");
                }
            }
            return true;
        });
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
                // Stop the tone after a short delay (e.g., 500ms)
                new android.os.Handler().postDelayed(() -> {
                    toneGen.stopTone();
                }, 500);
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(200);
                    }
                }
                Toast.makeText(SellProduct.this, "Product not found", Toast.LENGTH_SHORT).show();

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
                if (p.getBarcode().equals(scannedCode)) {
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
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(200);
                    }
                }
                Toast.makeText(SellProduct.this, "Product not found", Toast.LENGTH_SHORT).show();
            }
        }
    };

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