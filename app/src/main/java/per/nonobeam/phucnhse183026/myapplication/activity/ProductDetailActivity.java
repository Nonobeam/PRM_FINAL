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
    private TextView count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        TextView txtName = findViewById(R.id.txtNameDetail);
        TextView txtPrice = findViewById(R.id.txtPriceDetail);
        TextView txtDesc = findViewById(R.id.txtDescDetail);
        count = findViewById(R.id.count);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        edtQuantity = findViewById(R.id.edtQuantity);

        db = new DatabaseHelper(this);

        try {
            int userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("userId", -1);
            // Get product ID from intent
            productId = getIntent().getIntExtra("product_id", -1);
            Product product = db.getProductById(productId);
            if (productId != -1) {
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
                    if (quantity <= 0) {
                        Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    quantity = 1;
                }
                int currentCartQuantity = db.getCartQuantity(userId, productId);
                int productQuantity = db.getProductById(productId).quantity;
                boolean added = db.addToCart(userId, productId, quantity);
                if (added) {
                    count.setText("("+db.getCartItemCount(userId)+")");
                    if (currentCartQuantity > 0) {
                        if (currentCartQuantity + quantity <= productQuantity){
                            int newQuantity = currentCartQuantity + quantity;
                            Toast.makeText(this,
                                    String.format("Updated quantity to %d in cart", newQuantity),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            db.addToCart(userId, productId, -1);
                            Toast.makeText(this, "Product is out of stock", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                    }
                    edtQuantity.setText("1");

                } else {
                    Toast.makeText(this, "Product is out of stock", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("userId", -1);
        int cartItemCount = db.getCartItemCount(userId);
        count.setText("(" + cartItemCount + ")");
    }
}