package per.nonobeam.phucnhse183026.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    // QR Code và Payment UI components
    private ImageView qrCodeImageView;
    private TextView paymentStatusText;
    private ProgressBar paymentProgressBar;
    private View paymentSummaryLayout;
    private View qrPaymentLayout;
    private Button btnPay;
    private Button btnCancelPayment;

    private String currentPaymentId;
    private Handler paymentHandler;
    private Runnable paymentTimeoutRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initializeComponents();
        setupPaymentSummary();
        setupClickListeners();
    }

    private void initializeComponents() {
        db = new DatabaseHelper(this);
        paymentHandler = new Handler(Looper.getMainLooper());

        // Existing components
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPayment);
        TextView txtTotal = findViewById(R.id.txtTotalPayment);
        TextView txtShipping = findViewById(R.id.txtShipping);
        TextView txtGrandTotal = findViewById(R.id.txtGrandTotal);
        btnPay = findViewById(R.id.btnPay);

        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        paymentStatusText = findViewById(R.id.paymentStatusText);
        paymentProgressBar = findViewById(R.id.paymentProgressBar);
        paymentSummaryLayout = findViewById(R.id.paymentSummaryLayout);
        qrPaymentLayout = findViewById(R.id.qrPaymentLayout);
        btnCancelPayment = findViewById(R.id.btnCancelPayment);

        // Setup RecyclerView
        selectedProducts = getIntent().getParcelableArrayListExtra("selected_products");
        userId = String.valueOf(getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("userId", -1));

        if (selectedProducts == null) {
            selectedProducts = new ArrayList<>();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PaymentProductAdapter(selectedProducts));

        // Calculate total
        for (Product p : selectedProducts) {
            totalProductPrice += p.price * p.quantity;
        }

        txtTotal.setText("Tổng tiền sản phẩm: $" + String.format("%.2f", totalProductPrice));
        txtShipping.setText("Phí vận chuyển: $" + String.format("%.2f", SHIPPING_FEE));
        txtGrandTotal.setText("Tổng thanh toán: $" + String.format("%.2f", totalProductPrice + SHIPPING_FEE));
    }

    private void setupPaymentSummary() {
        // Initially show payment summary, hide QR layout
        paymentSummaryLayout.setVisibility(View.VISIBLE);
        qrPaymentLayout.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnPay.setOnClickListener(v -> initiateQRPayment());

        btnCancelPayment.setOnClickListener(v -> {
            cancelPayment();
            showPaymentSummary();
        });
    }

    private void initiateQRPayment() {
        // Generate unique payment ID
        currentPaymentId = "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create QR code data
        String qrData = createQRPaymentData();

        // Generate and display QR code
        Bitmap qrBitmap = generateQRCode(qrData);
        if (qrBitmap != null) {
            showQRPaymentScreen(qrBitmap);
            startPaymentSimulation();
        } else {
            Toast.makeText(this, "Không thể tạo mã QR. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private String createQRPaymentData() {
        // Tạo dữ liệu QR theo format thực tế (VietQR hoặc format tùy chỉnh)
        return String.format(
                "PAYMENT_ID:%s|AMOUNT:%.2f|MERCHANT:MyApp|TIME:%d",
                currentPaymentId,
                totalProductPrice + SHIPPING_FEE,
                System.currentTimeMillis()
        );
    }

    private Bitmap generateQRCode(String data) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 300);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            Log.e("QRCode", "Error generating QR code", e);
            return null;
        }
    }

    private void showQRPaymentScreen(Bitmap qrBitmap) {
        // Hide payment summary, show QR payment screen
        paymentSummaryLayout.setVisibility(View.GONE);
        qrPaymentLayout.setVisibility(View.VISIBLE);

        // Display QR code
        qrCodeImageView.setImageBitmap(qrBitmap);

        // Update status
        paymentStatusText.setText("Quét mã QR để thanh toán\nSố tiền: $" +
                String.format("%.2f", totalProductPrice + SHIPPING_FEE));

        // Show progress bar
        paymentProgressBar.setVisibility(View.VISIBLE);

        // Set timeout for payment (60 seconds)
        paymentTimeoutRunnable = () -> {
            Toast.makeText(PaymentActivity.this, "Thanh toán đã hết thời gian!", Toast.LENGTH_SHORT).show();
            showPaymentSummary();
        };
        paymentHandler.postDelayed(paymentTimeoutRunnable, 60000); // 60 seconds timeout
    }

    private void startPaymentSimulation() {
        // Simulate payment processing after 3-5 seconds
        int delay = 3000 + (int)(Math.random() * 2000); // 3-5 seconds random delay

        paymentHandler.postDelayed(() -> {
            // Simulate QR scan detection
            paymentStatusText.setText("Đã quét mã QR!\nĐang xử lý thanh toán...");

            // Process payment after another 2 seconds
            paymentHandler.postDelayed(() -> {
                processPaymentWithMockAPI();
            }, 2000);

        }, delay);
    }

    private void processPaymentWithMockAPI() {
        PaymentRequest request = new PaymentRequest();
        request.orderId = "ORDER_" + System.currentTimeMillis();
        request.amount = totalProductPrice + SHIPPING_FEE;
        request.paymentMethod = "qr_code";
        request.paymentId = currentPaymentId;

        // Mock API call with JSONPlaceholder
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PaymentApi api = retrofit.create(PaymentApi.class);

        api.simulatePayment(request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                paymentHandler.removeCallbacks(paymentTimeoutRunnable);

                if (response.isSuccessful()) {
                    // Simulate 95% success rate
                    boolean paymentSuccess = Math.random() > 0.05;

                    if (paymentSuccess) {
                        handlePaymentSuccess(request);
                    } else {
                        handlePaymentFailure("Thanh toán thất bại! Vui lòng thử lại.");
                    }
                } else {
                    handlePaymentFailure("Lỗi hệ thống thanh toán! Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                paymentHandler.removeCallbacks(paymentTimeoutRunnable);
                handlePaymentFailure("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void handlePaymentSuccess(PaymentRequest request) {
        paymentProgressBar.setVisibility(View.GONE);
        paymentStatusText.setText("✅ Thanh toán thành công!\nMã giao dịch: " + currentPaymentId);

        // Process checkout in database
        boolean checkoutSuccess = db.processCheckout(selectedProducts, userId);

        if (checkoutSuccess) {
            for (Product product : selectedProducts) {
                db.deleteCartItem(Integer.parseInt(userId), product.id);
            }

            Order order = createOrder(request.orderId, request.amount, userId);
            boolean orderSaved = db.saveOrder(order);

            if (orderSaved) {
                paymentHandler.postDelayed(() -> {
                    Toast.makeText(PaymentActivity.this,
                            "Đơn hàng đã được tạo thành công!\nMã đơn hàng: " + request.orderId,
                            Toast.LENGTH_LONG).show();
                    navigateToCart();
                }, 1000);
            } else {
                handlePaymentFailure("Thanh toán thành công nhưng không thể lưu đơn hàng!");
            }
        } else {
            handlePaymentFailure("Thanh toán thành công nhưng không thể cập nhật kho!");
        }
    }

    private void handlePaymentFailure(String message) {
        paymentProgressBar.setVisibility(View.GONE);
        paymentStatusText.setText("❌ " + message);

        // Show retry option after 3 seconds
        paymentHandler.postDelayed(() -> {
            showPaymentSummary();
        }, 3000);
    }

    private void showPaymentSummary() {
        qrPaymentLayout.setVisibility(View.GONE);
        paymentSummaryLayout.setVisibility(View.VISIBLE);
        cancelPayment();
    }

    private void cancelPayment() {
        if (paymentTimeoutRunnable != null) {
            paymentHandler.removeCallbacks(paymentTimeoutRunnable);
        }
        currentPaymentId = null;
    }

    private Order createOrder(String orderId, double totalAmount, String userId) {
        Order order = new Order();
        order.orderId = orderId;
        order.amount = totalProductPrice;
        order.totalFee = SHIPPING_FEE;
        order.userId = userId;
        order.orderDate = System.currentTimeMillis();
        order.status = "COMPLETED";
        order.paymentId = currentPaymentId;

        // Create OrderItem list from selectedProducts
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPayment();
    }
    private void navigateToCart() {
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}