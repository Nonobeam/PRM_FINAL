package per.nonobeam.phucnhse183026.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.adapter.CartAdapter;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class CartActivity extends BaseActivity implements CartAdapter.OnCartUpdateListener {
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private TextView txtTotal;
    private Button btnCheckout;
    private List<Product> cartList;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = new DatabaseHelper(this);

        initViews();
        loadCartItems();
        setupRecyclerView();
    }

    private void initViews() {
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        txtTotal = findViewById(R.id.txtTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> processCheckout());
    }

    // Add null checks
    private void loadCartItems() {
        try {
            cartList = db.getCartItems();
            if (cartList == null) {
                cartList = new ArrayList<>();
            }
            if (cartAdapter != null) {
                cartAdapter.notifyDataSetChanged();
            }
            updateTotal();
        } catch (Exception e) {
            e.printStackTrace();
            cartList = new ArrayList<>();
        }
    }
    private void updateTotal() {
        double total = 0;
        for (Product product : cartList) {
            total += product.price * product.quantity;
        }
        txtTotal.setText(String.format("Total: $%.2f", total));
    }

    private void setupRecyclerView() {
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartList, this);
        recyclerViewCart.setAdapter(cartAdapter);
    }

    @Override
    public void onItemSelected(List<Product> selectedItems, double total) {
        txtTotal.setText(String.format("Total: $%.2f", total));
        btnCheckout.setEnabled(!selectedItems.isEmpty());
    }

    @Override
    public void onQuantityChanged(int position, int newQuantity) {
        Product product = cartList.get(position);
        if (newQuantity > 0) {
            product.quantity = newQuantity;
            if (db.updateCartItemQuantity(product.id, newQuantity)) {
                updateTotal();
            } else {
                Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
        }
    }

    private void processCheckout() {
        List<Product> selectedProducts = cartAdapter.getSelectedItems();
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PaymentActivity.class);

        ArrayList<Product> selectedProductsArray = new ArrayList<>(selectedProducts);

        intent.putParcelableArrayListExtra("selected_products", selectedProductsArray);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems(); // Refresh cart when returning to activity
    }
}