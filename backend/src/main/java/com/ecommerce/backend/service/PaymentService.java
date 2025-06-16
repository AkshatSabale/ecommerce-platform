package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.PaymentResponse;
import com.ecommerce.backend.model.Payment;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.PaymentRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

  private final RazorpayClient razorpayClient;
  private final PaymentRepository paymentRepository;
  private final UserRepository userRepository;

  @Value("${razorpay.key_secret}")
  private String keySecret;

  @Transactional
  public Payment createAndSavePaymentOrder(Double amount, String currency, Long userId)
      throws RazorpayException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    JSONObject options = new JSONObject();
    options.put("amount", (int)(amount * 100));
    options.put("currency", currency);
    options.put("receipt", "order_" + System.currentTimeMillis());

    Order razorpayOrder = razorpayClient.orders.create(options);

    Payment payment = new Payment();
    payment.setRazorpayOrderId(razorpayOrder.get("id"));
    payment.setAmount(amount);
    payment.setCurrency(currency);
    payment.setStatus("created");
    payment.setReceipt(razorpayOrder.get("receipt"));
    payment.setUser(user);
    // paymentId is intentionally left null until payment completes
    return paymentRepository.save(payment);
  }

  public Payment getPayment(String paymentId) {
    return paymentRepository.findByRazorpayOrderId(paymentId)
        .orElseThrow(() -> new RuntimeException("Payment not found"));
  }

  public Page<PaymentResponse> getUserPayments(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    Page<Payment> paymentPage = paymentRepository.findByUser_Id(userId, pageable);

    // Map Payment -> PaymentResponse
    return paymentPage.map(this::mapToPaymentResponse);
  }

  private PaymentResponse mapToPaymentResponse(Payment payment) {
    PaymentResponse response = new PaymentResponse();
    response.setId(payment.getId());
    response.setPaymentId(payment.getPaymentId());
    response.setAmount(payment.getAmount());
    response.setCurrency(payment.getCurrency());
    response.setStatus(payment.getStatus());
    response.setCreatedAt(payment.getCreatedAt().toString());
    response.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
    return response;
  }


  public Payment getPaymentForUser(String paymentId, Long userId) {
    Payment payment = paymentRepository.findByRazorpayOrderId(paymentId)
        .orElseThrow(() -> new RuntimeException("Payment not found"));

    if (!payment.getUser().getId().equals(userId)) {
      throw new RuntimeException("Payment not found or access denied");
    }

    return payment;
  }

  public boolean verifyAndCompletePayment(String orderId, String paymentId, String signature, Long userId)
      throws RazorpayException {

    boolean isValid = verifySignature(orderId, paymentId, signature);

    if (!isValid) return false;

    // Fetch existing payment by razorpayOrderId and userId
    Optional<Payment> optionalPayment = paymentRepository.findByRazorpayOrderId(orderId);

    if (optionalPayment.isEmpty()) {
      // Log this instead of throwing so it doesn't insert duplicate
      log.error("Payment record not found for Razorpay orderId: {}", orderId);
      return false;
    }

    Payment payment = optionalPayment.get();

    com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(paymentId);

    // Update payment fields
    payment.setPaymentId(paymentId);
    payment.setStatus(razorpayPayment.get("status"));     // e.g. "captured"
    payment.setMethod(razorpayPayment.get("method"));     // e.g. "netbanking"
    payment.setUpdatedAt(LocalDateTime.now());

    paymentRepository.save(payment);  // This updates the existing row

    return true;
  }

  @Transactional
  public void handleWebhookEvent(String payload, String signature) {
    try {
      String expectedSignature = hmacSha256(payload, keySecret);
      if (!expectedSignature.equals(signature)) {
        throw new SecurityException("Invalid signature");
      }

      JSONObject eventData = new JSONObject(payload);
      String event = eventData.getString("event");

      if ("payment.captured".equals(event)) {
        JSONObject paymentJson = eventData.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        updatePaymentFromWebhook(paymentJson);
      }

    } catch (Exception e) {
      log.error("Webhook processing failed", e);
      throw new RuntimeException("Webhook processing failed");
    }
  }

  private void updatePaymentFromWebhook(JSONObject paymentJson) {
    String paymentId = paymentJson.getString("id");
    Payment payment = paymentRepository.findByRazorpayOrderId(paymentId)
        .orElseGet(() -> {
          Payment newPayment = new Payment();
          newPayment.setPaymentId(paymentId);
          // You might want to set other required fields or throw an exception
          return newPayment;
        });

    payment.setStatus(paymentJson.getString("status"));
    payment.setAmount(paymentJson.getDouble("amount") / 100);
    payment.setCurrency(paymentJson.getString("currency"));
    payment.setMethod(paymentJson.getString("method"));
    // Set other fields as needed

    paymentRepository.save(payment);
  }


  private boolean verifySignature(String orderId, String paymentId, String razorpaySignature) {
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