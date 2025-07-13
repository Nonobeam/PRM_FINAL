package per.nonobeam.phucnhse183026.myapplication.model;

public class PaymentRequest {
    public String orderId;
    public double amount;
    public String paymentMethod;
    public String paymentId;
    public String userId;
    public long timestamp;

    public PaymentRequest() {
        this.timestamp = System.currentTimeMillis();
    }
}