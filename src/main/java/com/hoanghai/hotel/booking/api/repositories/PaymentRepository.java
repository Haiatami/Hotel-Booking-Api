package com.hoanghai.hotel.booking.api.repositories;

import com.hoanghai.hotel.booking.api.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
