package per.nonobeam.phucnhse183026.myapplication.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<Product> products;
    private OnCartUpdateListener listener;
    private Map<Integer, Boolean> selectedItems;

    public CartAdapter(Context context, List<Product> products, OnCartUpdateListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
        this.selectedItems = new HashMap<>();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = products.get(position);

        holder.txtName.setText(product.name);
        holder.txtPrice.setText(String.format("$%.2f", product.price));
        holder.edtQuantity.setText(String.valueOf(product.quantity));

        holder.checkBox.setChecked(selectedItems.containsKey(holder.getAdapterPosition()));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            if (isChecked) {
                selectedItems.put(currentPosition, true);
            } else {
                selectedItems.remove(currentPosition);
            }
            updateTotalPrice();
        });


        if (holder.quantityTextWatcher != null) {
            holder.edtQuantity.removeTextChangedListener(holder.quantityTextWatcher);
        }

        holder.quantityTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                try {
                    if (listener != null) {
                        int newQuantity = Integer.parseInt(s.toString());
                        if (currentPosition < products.size()) { // Kiểm tra để tránh IndexOutOfBounds
                        }
                        listener.onQuantityChanged(currentPosition, newQuantity);
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu nhập không phải là số
                }
            }
        };
        holder.edtQuantity.addTextChangedListener(holder.quantityTextWatcher);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public List<Product> getSelectedItems() {
        List<Product> selected = new ArrayList<>();

        for (Map.Entry<Integer, Boolean> entry : selectedItems.entrySet()) {
            int itemPosition = entry.getKey();
            if (entry.getValue() && itemPosition >= 0 && itemPosition < products.size()) {
                selected.add(products.get(itemPosition));
            }
        }
        return selected;
    }

    private void updateTotalPrice() {
        double total = 0;
        for (Map.Entry<Integer, Boolean> entry : selectedItems.entrySet()) {
            int itemPosition = entry.getKey();
            if (entry.getValue() && itemPosition >= 0 && itemPosition < products.size()) {
                Product product = products.get(itemPosition);
                total += product.price * product.quantity; // Giả sử product.quantity đã được cập nhật
            }
        }
        if (listener != null) {
            listener.onItemSelected(getSelectedItems(), total);
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView txtName;
        TextView txtPrice;
        EditText edtQuantity;
        TextWatcher quantityTextWatcher;

        CartViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxItem);
            txtName = itemView.findViewById(R.id.txtCartItemName);
            txtPrice = itemView.findViewById(R.id.txtCartItemPrice);
            edtQuantity = itemView.findViewById(R.id.edtCartQuantity);
        }
    }

    public interface OnCartUpdateListener {
        void onItemSelected(List<Product> selectedItems, double total);

        void onQuantityChanged(int position, int newQuantity);
    }

    public void updateProductQuantity(int position, int newQuantity) {
        if (position >= 0 && position < products.size()) {
            products.get(position).quantity = newQuantity;
            if (selectedItems.containsKey(position) && selectedItems.get(position)) {
                updateTotalPrice();
            }
        }
    }
}