package per.nonobeam.phucnhse183026.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.adapter.CartAdapter;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class CartActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView txtTotal;
    DatabaseHelper db;
    List<Product> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (item.getItemId() == R.id.topAppBar) {
                startActivity(new Intent(this, ProductListActivity.class));
                return true;
            }
            return false;
        });

        recyclerView = findViewById(R.id.recyclerViewCart);
        txtTotal = findViewById(R.id.txtTotal);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);
        cartItems = db.getCartProducts();

        recyclerView.setAdapter(new CartAdapter(this, cartItems));

        double total = 0;
        for (Product p : cartItems) {
            total += p.price * p.quantity;
        }

        txtTotal.setText("Total: $" + String.format("%.2f", total));
    }
}
