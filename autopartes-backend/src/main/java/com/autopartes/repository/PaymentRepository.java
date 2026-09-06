package com.autopartes.repository;

import com.autopartes.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByMercadoPagoPaymentId(String paymentId);
    Optional<Payment> findByMercadoPagoPreferenceId(String preferenceId);
}
