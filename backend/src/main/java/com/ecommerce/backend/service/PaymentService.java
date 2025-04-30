package com.ecommerce.backend.service;



import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {

  @Value("${razorpay.key_id}")
  private String keyId;

  @Value("${razorpay.key_secret}")
  private String keySecret;

  public String createPaymentOrder(Double amount, String currency) throws RazorpayException {
    try {
      RazorpayClient client = new RazorpayClient(keyId, keySecret);

      JSONObject options = new JSONObject();
      options.put("amount",(int)(amount * 100));
      options.put("currency", currency);
      options.put("receipt", "ABCD");
      log.info("Creating Razorpay order with payload: {}", options);

      Order order = client.orders.create(options);

      return order.toString(); // Razorpay order response
    } catch (RazorpayException e) {
      e.printStackTrace();
      throw new RuntimeException("Payment order creation failed");
    }
  }

  public String getPaymentDetails(String paymentId) throws RazorpayException {
    RazorpayClient client = new RazorpayClient(keyId, keySecret);
    Payment payment = client.payments.fetch(paymentId);
    return payment.toString();
  }

  public String refundPayment(String paymentId, int amount) throws RazorpayException {
    RazorpayClient client = new RazorpayClient(keyId, keySecret);
    JSONObject refundRequest = new JSONObject();
    refundRequest.put("amount", amount);
    Refund refund = client.payments.refund(paymentId, refundRequest);
    return refund.toString();
  }

  public boolean verifySignature(String orderId, String paymentId, String razorpaySignature) {
    try {
      String payload = orderId + "|" + paymentId;
      String actualSignature = hmacSha256(payload, keySecret);
      return actualSignature.equals(razorpaySignature);
    } catch (Exception e) {
      log.error("Signature verification failed", e);
      return false;
    }
  }

  private String hmacSha256(String data, String key) throws Exception {
    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(secretKeySpec);
    byte[] hmacData = mac.doFinal(data.getBytes());
    return new String(Hex.encode(hmacData));
  }
}
