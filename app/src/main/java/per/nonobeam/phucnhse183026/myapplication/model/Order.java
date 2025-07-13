package per.nonobeam.phucnhse183026.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Order implements Parcelable {
    public String orderId;
    public double amount;
    public double totalFee;
    public String userId;
    public long orderDate;
    public String status;
    public List<OrderItem> orderItems;
    public String paymentId;

    public Order() {
        this.status = "PENDING";
        this.orderDate = System.currentTimeMillis();
    }
    protected Order(Parcel in) {
        orderId = in.readString();
        amount = in.readDouble();
        totalFee = in.readDouble();
        userId = in.readString();
        orderDate = in.readLong();
        status = in.readString();
        orderItems = in.createTypedArrayList(OrderItem.CREATOR);
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeDouble(amount);
        dest.writeDouble(totalFee);
        dest.writeString(userId);
        dest.writeLong(orderDate);
        dest.writeString(status);
        dest.writeTypedList(orderItems);
    }

    public double getTotalAmount() {
        return amount + totalFee;
    }

}
