package per.nonobeam.phucnhse183026.myapplication.api;

import per.nonobeam.phucnhse183026.myapplication.model.PaymentRequest;
import per.nonobeam.phucnhse183026.myapplication.model.PaymentResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApi {
    @POST("/api/payment/simulate")
    Call<PaymentResponse> simulatePayment(@Body PaymentRequest request);
}

