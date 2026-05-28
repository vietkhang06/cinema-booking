package com.cinemabooking.backend.payment.service;

import com.cinemabooking.backend.payment.model.Payment;
import com.cinemabooking.backend.payment.model.PaymentStatus;
import com.cinemabooking.backend.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPendingPayment(String bookingId, String userId, String provider, double amount) throws ExecutionException, InterruptedException {
        String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();

        String suffix = bookingId.contains("_") ? bookingId.substring(bookingId.indexOf("_") + 1) : bookingId;
        if (suffix.length() > 8) {
            suffix = suffix.substring(0, 8);
        }
        String paymentCode = ("BK" + suffix).toUpperCase();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .bookingId(bookingId)
                .paymentCode(paymentCode)
                .userId(userId)
                .provider(provider)
                .amount(amount)
                .status(PaymentStatus.PENDING.name())
                .transactionId("TXN_" + UUID.randomUUID().toString().substring(0, 8))
                .payUrl("http://fake-payurl.com/pay/" + paymentId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        log.info("[PAYMENT_FLOW] Creating payment record: paymentId={}, bookingId={}, paymentCode={}, amount={}, status=PENDING",
                paymentId, bookingId, paymentCode, amount);

        return paymentRepository.save(payment);
    }
}
