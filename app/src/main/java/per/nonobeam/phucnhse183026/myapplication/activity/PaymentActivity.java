package per.nonobeam.phucnhse183026.myapplication.activity;

import android.os.Bundle;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPayment);
        TextView txtTotal = findViewById(R.id.txtTotalPayment);
        TextView txtShipping = findViewById(R.id.txtShipping);
        TextView txtGrandTotal = findViewById(R.id.txtGrandTotal);
        Button btnPay = findViewById(R.id.btnPay);

        selectedProducts = getIntent().getParcelableArrayListExtra("selected_products");
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
            // Build PaymentRequest matching mock API
            PaymentRequest request = new PaymentRequest();
            request.orderId = "12345"; // You can generate or get this from your order system
            request.amount = totalProductPrice + SHIPPING_FEE;
            request.paymentMethod = "mock_card";

            // Setup Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://15e86d67-3e25-44f3-95db-d62b5e1bf2d3.mock.pstmn.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            PaymentApi api = retrofit.create(PaymentApi.class);

            // Call API
            api.simulatePayment(request).enqueue(new Callback<PaymentResponse>() {
                @Override
                public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(PaymentActivity.this, response.body().message + "\nPayment ID: " + response.body().paymentId, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Payment failed!", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<PaymentResponse> call, Throwable t) {
                    Toast.makeText(PaymentActivity.this, "Network error!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}

// Adapter đơn giản cho danh sách sản phẩm thanh toán
class PaymentProductAdapter extends RecyclerView.Adapter<PaymentProductAdapter.ViewHolder> {
    private List<Product> products;

    public PaymentProductAdapter(List<Product> products) {
        this.products = products;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Product p = products.get(position);
        holder.txt1.setText(p.name + " x" + p.quantity);
        holder.txt2.setText("$" + String.format("%.2f", p.price * p.quantity));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt1, txt2;

        ViewHolder(android.view.View itemView) {
            super(itemView);
            txt1 = itemView.findViewById(android.R.id.text1);
            txt2 = itemView.findViewById(android.R.id.text2);
        }
    }
}

