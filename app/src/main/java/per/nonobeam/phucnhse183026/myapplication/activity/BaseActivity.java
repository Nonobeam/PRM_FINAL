package per.nonobeam.phucnhse183026.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;

public class BaseActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                Intent intent = new Intent(this, ProductListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
            toolbar.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_cart) {
                    startActivity(new Intent(this, CartActivity.class));
                    return true;
                } else if (itemId == R.id.action_orders) {
                    startActivity(new Intent(this, OrderHistoryActivity.class));
                    return true;
                }
                return false;
            });
        }
    }
}
