package per.nonobeam.phucnhse183026.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class ProductDetailActivity extends BaseActivity {

    TextView txtName, txtPrice, txtDesc;
    Button btnAddToCart;
    DatabaseHelper db;
    Product product;
    int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        txtName = findViewById(R.id.txtNameDetail);
        txtPrice = findViewById(R.id.txtPriceDetail);
        txtDesc = findViewById(R.id.txtDescDetail);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        db = new DatabaseHelper(this);

        productId = getIntent().getIntExtra("product_id", -1);
        product = db.getProductById(productId);

        if (product != null) {
            txtName.setText(product.name);
            txtPrice.setText("Price: $" + product.price);
            txtDesc.setText(product.description);
        }

        btnAddToCart.setOnClickListener(v -> {
            boolean added = db.addToCart(productId, 1); // 1 quantity
            Toast.makeText(this, added ? "Added to cart" : "Already in cart", Toast.LENGTH_SHORT).show();
        });
    }
}
