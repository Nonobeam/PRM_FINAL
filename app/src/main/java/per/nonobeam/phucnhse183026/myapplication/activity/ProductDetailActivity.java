package per.nonobeam.phucnhse183026.myapplication.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class ProductDetailActivity extends BaseActivity {
    private DatabaseHelper db;
    private Button btnAddToCart;
    private EditText edtQuantity;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize views
        TextView txtName = findViewById(R.id.txtNameDetail);
        TextView txtPrice = findViewById(R.id.txtPriceDetail);
        TextView txtDesc = findViewById(R.id.txtDescDetail);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        edtQuantity = findViewById(R.id.edtQuantity);

        db = new DatabaseHelper(this);

        try {
            // Get product ID from intent
            productId = getIntent().getIntExtra("product_id", -1);
            if (productId != -1) {
                Product product = db.getProductById(productId);
                if (product != null) {
                    txtName.setText(product.name);
                    txtPrice.setText(String.format("$%.2f", product.price));
                    txtDesc.setText(product.description);
                }
            }

            btnAddToCart.setOnClickListener(v -> {
                int quantity = 1;
                try {
                    quantity = Integer.parseInt(edtQuantity.getText().toString());
                } catch (NumberFormatException e) {
                    quantity = 1;
                }
                boolean added = db.addToCart(productId, quantity);
                Toast.makeText(this, added ? "Added to cart" : "Already in cart", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}