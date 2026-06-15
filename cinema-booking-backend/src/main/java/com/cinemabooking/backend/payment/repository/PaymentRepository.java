/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Payment
 * 
 * Mô tả: 
 * Kho lưu trữ (Repository) cho các giao dịch thanh toán.
 * Giao tiếp trực tiếp với database để lưu lại lịch sử dòng tiền.
 */
package com.cinemabooking.backend.payment.repository;

import com.cinemabooking.backend.payment.model.Payment;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class PaymentRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "payments";

    public Payment save(Payment payment) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(payment.getPaymentId()).set(payment).get();
        return payment;
    }

    public Payment findById(String paymentId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION).document(paymentId).get().get().toObject(Payment.class);
    }
}
