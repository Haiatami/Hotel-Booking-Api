package com.hoanghai.hotel.booking.api.services;

import com.hoanghai.hotel.booking.api.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);

    void sendSms();

    void sendWhatsApp();
}
