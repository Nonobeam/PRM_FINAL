package per.nonobeam.phucnhse183026.myapplication.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.adapter.CartAdapter;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class CartActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView txtTotal;
    DatabaseHelper db;
    List<Product> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

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
