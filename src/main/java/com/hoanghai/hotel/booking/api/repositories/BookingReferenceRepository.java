package com.hoanghai.hotel.booking.api.repositories;

import com.hoanghai.hotel.booking.api.entities.BookingReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingReferenceRepository extends JpaRepository<BookingReference,Long> {
    Optional<BookingReference> findByReferenceNo(String referenceNo);
}
