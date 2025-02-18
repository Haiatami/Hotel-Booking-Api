package com.hoanghai.hotel.booking.api.repositories;

import com.hoanghai.hotel.booking.api.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}
