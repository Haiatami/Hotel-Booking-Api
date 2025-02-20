package com.hoanghai.hotel.booking.api.services;

import com.hoanghai.hotel.booking.api.dtos.BookingDTO;
import com.hoanghai.hotel.booking.api.dtos.Response;

public interface BookingService {
    Response createBooking(BookingDTO bookingDTO);

    Response getAllBookings();

    Response findBookingByReferenceNo(String bookingReference);

    Response updateBooking(BookingDTO bookingDTO);
}
