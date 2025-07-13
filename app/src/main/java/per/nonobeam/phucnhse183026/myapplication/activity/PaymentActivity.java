package per.nonobeam.phucnhse183026.myapplication.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.adapter.PaymentProductAdapter;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.Order;
import per.nonobeam.phucnhse183026.myapplication.model.OrderItem;
import per.nonobeam.phucnhse183026.myapplication.model.Product;
import per.nonobeam.phucnhse183026.myapplication.api.PaymentApi;
import per.nonobeam.phucnhse183026.myapplication.model.PaymentRequest;
import per.nonobeam.phucnhse183026.myapplication.model.PaymentResponse;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {
    private static final double SHIPPING_FEE = 30.0;
    private List<Product> selectedProducts;
    private double totalProductPrice = 0;
    DatabaseHelper db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        db = new DatabaseHelper(this);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPayment);
        TextView txtTotal = findViewById(R.id.txtTotalPayment);
        TextView txtShipping = findViewById(R.id.txtShipping);
        TextView txtGrandTotal = findViewById(R.id.txtGrandTotal);
        Button btnPay = findViewById(R.id.btnPay);
        selectedProducts = getIntent().getParcelableArrayListExtra("selected_products");
        userId = String.valueOf(getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("userId", -1));
        if (selectedProducts == null)
            selectedProducts = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PaymentProductAdapter(selectedProducts));
        for (Product p : selectedProducts) {
            totalProductPrice += p.price * p.quantity;
        }
        txtTotal.setText("Tổng tiền sản phẩm: $" + String.format("%.2f", totalProductPrice));
        txtShipping.setText("Phí vận chuyển: $" + String.format("%.2f", SHIPPING_FEE));
        txtGrandTotal.setText("Tổng thanh toán: $" + String.format("%.2f", totalProductPrice + SHIPPING_FEE));
        btnPay.setOnClickListener(v -> {
            processPaymentWithJSONPlaceholder();
        });
    }

    private void processPaymentWithJSONPlaceholder() {
        Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();
        PaymentRequest request = new PaymentRequest();
        request.orderId = "ORDER_" + System.currentTimeMillis();
        request.amount = totalProductPrice + SHIPPING_FEE;
        request.paymentMethod = "mock_card";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PaymentApi api = retrofit.create(PaymentApi.class);
        api.simulatePayment(request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse apiResponse = response.body();
//                    boolean paymentSuccess = Math.random() > 0.1;
                    boolean paymentSuccess = true;
                    if (paymentSuccess) {
                        boolean checkoutSuccess = db.processCheckout(selectedProducts, userId);
                        if (checkoutSuccess) {
                            Order order = createOrder(request.orderId, request.amount, userId);
                            boolean orderSaved = db.saveOrder(order);
                            if (orderSaved) {
                                Toast.makeText(PaymentActivity.this,
                                        "Payment successful!\nPayment ID: " + response.body().id +
                                                "\nOrder ID: " + response.body().orderId,
                                        Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(PaymentActivity.this,
                                        "Payment processed, but failed to save order!",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(PaymentActivity.this,
                                    "Payment successful but failed to update inventory!",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(PaymentActivity.this,
                                "Payment failed! Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(PaymentActivity.this,
                            "Payment failed! Error code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                Log.e("PaymentAPI", "Network error: " + t.getMessage(), t);
                Toast.makeText(PaymentActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private Order createOrder(String orderId, double totalAmount, String userId) {
        Order order = new Order();
        order.orderId = orderId;
        order.amount = totalProductPrice;
        order.totalFee = PaymentActivity.SHIPPING_FEE;
        order.userId = userId;
        order.orderDate = System.currentTimeMillis();
        order.status = "COMPLETED";

        // Tạo danh sách OrderItem từ selectedProducts
        List<OrderItem> orderItems = new ArrayList<>();
        for (Product product : selectedProducts) {
            OrderItem item = new OrderItem();
            item.productId = product.id;
            item.productName = product.name;
            item.quantity = product.quantity;
            item.price = product.price;
            item.totalPrice = product.price * product.quantity;
            orderItems.add(item);
        }
        order.orderItems = orderItems;

        return order;
    }
}

