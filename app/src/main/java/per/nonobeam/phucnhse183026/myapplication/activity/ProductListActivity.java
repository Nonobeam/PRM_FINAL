package per.nonobeam.phucnhse183026.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.adapter.ProductAdapter;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class ProductListActivity extends BaseActivity {

    RecyclerView recyclerView;
    ProductAdapter productAdapter;
    DatabaseHelper db;
    List<Product> productList;

    FloatingActionButton fabMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        db = new DatabaseHelper(this);
        productList = db.getAllProducts();

        if (productList.isEmpty()) {
            Toast.makeText(this, "No products found!", Toast.LENGTH_SHORT).show();
        }

        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);
        fabMap = findViewById(R.id.fab_maps);

        fabMap.setOnClickListener(v -> {
            // Địa chỉ cửa hàng ví dụ (mày có thể thay đổi)
            // Ví dụ: FPT Aptech International Academy, Vietnam
            String address = "Dai hoc FPT, Ho Chi Minh City, Vietnam";
            // Hoặc có thể dùng kinh độ, vĩ độ nếu có: String uri = "geo:10.794276,106.666352?q=FPT+Aptech+International+Academy";
            String uri = "geo:0,0?q=" + Uri.encode(address);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            // Đặt package để đảm bảo mở bằng Google Maps app, nếu có
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                // Nếu có ứng dụng Google Maps để xử lý Intent
                startActivity(mapIntent);
            } else {
                // Nếu không có ứng dụng Google Maps, thông báo cho người dùng
                Toast.makeText(ProductListActivity.this, "Google Maps app is not installed.", Toast.LENGTH_SHORT).show();
                // Hoặc mở bằng trình duyệt web
                String webUri = "https://maps.google.com/?q=" + Uri.encode(address);
                Intent webMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                startActivity(webMapIntent);
            }
        });
    }
}