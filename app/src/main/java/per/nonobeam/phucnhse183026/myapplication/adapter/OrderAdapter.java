package per.nonobeam.phucnhse183026.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.model.Order;
import per.nonobeam.phucnhse183026.myapplication.model.OrderItem;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context context;
    private SimpleDateFormat dateFormat;

    public OrderAdapter(List<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.tvOrderId.setText("Order ID: " + order.orderId);
        holder.tvOrderDate.setText("Date: " + dateFormat.format(new Date(order.orderDate)));
        holder.tvOrderStatus.setText("Status: " + order.status);
        holder.tvOrderAmount.setText("Amount: $" + String.format("%.2f", order.amount));
        holder.tvOrderTotalFee.setText("Total: $" + String.format("%.2f", order.totalFee));

        // Hiển thị items
        StringBuilder itemsText = new StringBuilder();
        for (OrderItem item : order.orderItems) {
            itemsText.append("• ")
                    .append(item.productName)
                    .append(" (x")
                    .append(item.quantity)
                    .append(") - $")
                    .append(String.format("%.2f", item.totalPrice))
                    .append("\n");
        }
        holder.tvOrderItems.setText(itemsText.toString());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderAmount, tvOrderTotalFee, tvOrderItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvOrderTotalFee = itemView.findViewById(R.id.tvOrderTotalFee);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
        }
    }
}