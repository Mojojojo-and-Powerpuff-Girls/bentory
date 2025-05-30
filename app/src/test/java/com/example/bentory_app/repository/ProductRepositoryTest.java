package com.example.bentory_app.repository;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.example.bentory_app.model.ProductModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class ProductRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private DatabaseReference mockDatabaseReference;

    @Mock
    private DataSnapshot mockRootDataSnapshot; // fake data
    @Mock
    private DataSnapshot mockChildDataSnapshot1;
    @Mock
    private DataSnapshot mockChildDataSnapshot2;

    @Mock
    private Observer<List<ProductModel>> mockObserver;

    @Mock
    private DatabaseReference mockChildReference;

    @Mock
    private ProductRepository.ProductCallback mockCallback;


    @Captor
    private ArgumentCaptor<ProductModel> productCaptor;

    private MutableLiveData<List<ProductModel>> testLiveData;
    private ProductRepository productRepository;
    private ValueEventListener injectedListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        injectedListener = new ValueEventListener() {
            final List<ProductModel> listData = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listData.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ProductModel productModel = dataSnapshot.getValue(ProductModel.class);
                        if (productModel != null) {
                            productModel.setId(dataSnapshot.getKey());
                            listData.add(productModel);
                        }
                    }
                    testLiveData.setValue(listData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional error logic
            }
        };

        productRepository = new ProductRepository(mockDatabaseReference, injectedListener);
        testLiveData = (MutableLiveData<List<ProductModel>>) productRepository.getData();
    }

    @Test
    public void getData_whenDataExists_postsDataToLiveData() {
        testLiveData.observeForever(mockObserver);

        when(mockRootDataSnapshot.exists()).thenReturn(true);
        when(mockRootDataSnapshot.getChildren()).thenReturn(Arrays.asList(mockChildDataSnapshot1, mockChildDataSnapshot2));

        ProductModel expected1 = new ProductModel();
        expected1.setName("Product 1");
        when(mockChildDataSnapshot1.getValue(ProductModel.class)).thenReturn(expected1);
        when(mockChildDataSnapshot1.getKey()).thenReturn("key1");

        ProductModel expected2 = new ProductModel();
        expected2.setName("Product 2");
        when(mockChildDataSnapshot2.getValue(ProductModel.class)).thenReturn(expected2);
        when(mockChildDataSnapshot2.getKey()).thenReturn("key2");

        // Manually trigger the listener's onDataChange
        injectedListener.onDataChange(mockRootDataSnapshot);

        ArgumentCaptor<List<ProductModel>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockObserver).onChanged(captor.capture());
        List<ProductModel> result = captor.getValue();

        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("key1", result.get(0).getId());
        assertEquals("Product 2", result.get(1).getName());
        assertEquals("key2", result.get(1).getId());

        testLiveData.removeObserver(mockObserver);
    }




    @Test
    public void addProduct_shouldAddWithCorrectKeyAndTimestamp() {
        ProductModel product = new ProductModel();
        product.setName("Test Product");

        // Simulate existing keys in DB: item1 and item3
        when(mockRootDataSnapshot.getChildren()).thenReturn(Arrays.asList(mockChildDataSnapshot1, mockChildDataSnapshot2));
        when(mockChildDataSnapshot1.getKey()).thenReturn("item1");
        when(mockChildDataSnapshot2.getKey()).thenReturn("item3");

        // Mock the returned child reference when we call database.child("item4")
        when(mockDatabaseReference.child(anyString())).thenReturn(mockChildReference);

        //Trigger addProduct();
        productRepository.addProduct(product);

        // Capture the listener passed to addListenerForSingleValueEvent
        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        verify(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture());

        // Simulate the listener being triggered with the mocked snapshot
        listenerCaptor.getValue().onDataChange(mockRootDataSnapshot);

        // Capture the saved product
        verify(mockChildReference).setValue(productCaptor.capture());
        ProductModel savedProduct = productCaptor.getValue();

        assertEquals("Test Product", savedProduct.getName());
        assertNotNull(savedProduct.getDate_Added());
        assertTrue(savedProduct.getDate_Added().contains(", 20")); // Loose check for formatted date
    }

    private ProductModel makeProduct(String id, String name) {
        ProductModel p = new ProductModel();
        p.setId(id);
        p.setName(name);
        return p;
    }

    @Test
    public void testDeleteProductsByIds_andReassignKeys() {
        Set<String> idsToDelete = new HashSet<>(Arrays.asList("item1", "item3"));

        ProductModel product2 = new ProductModel("item2", "Product B", 15.0, 8);
        ProductModel product4 = new ProductModel("item4", "Product D", 25.0, 3);

        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        DataSnapshot mockChild2 = mock(DataSnapshot.class);
        DataSnapshot mockChild4 = mock(DataSnapshot.class);

        when(mockSnapshot.getChildren()).thenReturn(Arrays.asList(mockChild2, mockChild4));
        when(mockChild2.getKey()).thenReturn("item2");
        when(mockChild2.getValue(ProductModel.class)).thenReturn(product2);
        when(mockChild4.getKey()).thenReturn("item4");
        when(mockChild4.getValue(ProductModel.class)).thenReturn(product4);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);

        Map<String, DatabaseReference> childRefs = new HashMap<>();
        when(mockDatabaseReference.child(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return childRefs.computeIfAbsent(key, k -> mock(DatabaseReference.class));
        });

        doNothing().when(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture());

        // Mock removeValue and task completion
        Task<Void> mockRemoveTask = mock(Task.class);
        when(mockDatabaseReference.removeValue()).thenReturn(mockRemoveTask);
        when(mockRemoveTask.isSuccessful()).thenReturn(true);
        when(mockRemoveTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(mockRemoveTask);
            return null;
        });

        productRepository.deleteProductsByIds(idsToDelete);

        ValueEventListener capturedListener = listenerCaptor.getValue();
        capturedListener.onDataChange(mockSnapshot);

        // Verify the reassigned product writes
        ProductModel expected1 = new ProductModel("item1", "Product B", 15.0, 8);
        ProductModel expected2 = new ProductModel("item2", "Product D", 25.0, 3);

        verify(childRefs.get("item1")).setValue(refEq(expected1));
        verify(childRefs.get("item2")).setValue(refEq(expected2));
    }


    @Test
    public void testGetProductByMatchingBarcode_found() {
        String barcodeToFind = "1234567890";

        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        Iterable<DataSnapshot> children = java.util.Collections.singletonList(childSnapshot);

        when(mockSnapshot.getChildren()).thenReturn(children);
        when(childSnapshot.getValue(ProductModel.class)).thenAnswer(invocation -> {
            ProductModel product = new ProductModel();
            product.setBarcode(Arrays.asList("1234567890", "9876543210"));
            return product;
        });

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        doNothing().when(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture());

        productRepository.getProductByMatchingBarcode(barcodeToFind, mockCallback);

        // Trigger the listener manually simulating data load
        listenerCaptor.getValue().onDataChange(mockSnapshot);

        verify(mockCallback).onProductFound(any(ProductModel.class));
        verify(mockCallback, never()).onProductNotFound();
        verify(mockCallback, never()).onError(anyString());
    }


    @Test
    public void testGetProductByMatchingBarcode_notFound() {
        String barcodeToFind = "0000000000";

        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        Iterable<DataSnapshot> children = java.util.Collections.emptyList();

        when(mockSnapshot.getChildren()).thenReturn(children);

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        doNothing().when(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture());

        productRepository.getProductByMatchingBarcode(barcodeToFind, mockCallback);

        listenerCaptor.getValue().onDataChange(mockSnapshot);

        verify(mockCallback).onProductNotFound();
        verify(mockCallback, never()).onProductFound(any());
        verify(mockCallback, never()).onError(anyString());
    }


    @Test
    public void testGetProductByMatchingBarcode_onCancelled() {
        String errorMessage = "Permission denied";

        ArgumentCaptor<ValueEventListener> listenerCaptor = ArgumentCaptor.forClass(ValueEventListener.class);
        doNothing().when(mockDatabaseReference).addListenerForSingleValueEvent(listenerCaptor.capture());

        productRepository.getProductByMatchingBarcode("anybarcode", mockCallback);

        DatabaseError mockError = mock(DatabaseError.class);
        when(mockError.getMessage()).thenReturn(errorMessage);

        listenerCaptor.getValue().onCancelled(mockError);

        verify(mockCallback).onError(errorMessage);
        verify(mockCallback, never()).onProductFound(any());
        verify(mockCallback, never()).onProductNotFound();
    }

}

