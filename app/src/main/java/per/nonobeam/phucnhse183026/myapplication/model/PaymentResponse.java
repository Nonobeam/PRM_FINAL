package per.nonobeam.phucnhse183026.myapplication.model;


public class PaymentResponse {
    public int id;
    public String orderId;
    public String paymentId;
    public String status;
    public double amount;
    public String message;
    public long timestamp;

    public PaymentResponse() {
        this.timestamp = System.currentTimeMillis();
        this.status = "SUCCESS";
        this.message = "Payment processed successfully";
    }
}