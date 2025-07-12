package per.nonobeam.phucnhse183026.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    public int productId;
    public String productName;
    public int quantity;
    public double price;
    public double totalPrice;

    public OrderItem() {}

    protected OrderItem(Parcel in) {
        productId = in.readInt();
        productName = in.readString();
        quantity = in.readInt();
        price = in.readDouble();
        totalPrice = in.readDouble();
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(productId);
        dest.writeString(productName);
        dest.writeInt(quantity);
        dest.writeDouble(price);
        dest.writeDouble(totalPrice);
    }
}
