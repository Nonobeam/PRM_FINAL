package per.nonobeam.phucnhse183026.myapplication.activity;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import per.nonobeam.phucnhse183026.myapplication.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
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
                }

                return false;
            });
        }
    }
}
