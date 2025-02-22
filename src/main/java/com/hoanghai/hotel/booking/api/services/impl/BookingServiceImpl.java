package com.hoanghai.hotel.booking.api.services.impl;

import com.hoanghai.hotel.booking.api.dtos.BookingDTO;
import com.hoanghai.hotel.booking.api.dtos.NotificationDTO;
import com.hoanghai.hotel.booking.api.dtos.Response;
import com.hoanghai.hotel.booking.api.entities.Booking;
import com.hoanghai.hotel.booking.api.entities.Room;
import com.hoanghai.hotel.booking.api.entities.User;
import com.hoanghai.hotel.booking.api.enums.BookingStatus;
import com.hoanghai.hotel.booking.api.enums.PaymentStatus;
import com.hoanghai.hotel.booking.api.exceptions.InvalidBookingStateAndDateException;
import com.hoanghai.hotel.booking.api.exceptions.NotFoundException;
import com.hoanghai.hotel.booking.api.repositories.BookingRepository;
import com.hoanghai.hotel.booking.api.repositories.RoomRepository;
import com.hoanghai.hotel.booking.api.services.BookingCodeGenerator;
import com.hoanghai.hotel.booking.api.services.BookingService;
import com.hoanghai.hotel.booking.api.services.NotificationService;
import com.hoanghai.hotel.booking.api.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;

    private final RoomRepository roomRepository;

    private final NotificationService notificationService;

    private final ModelMapper modelMapper;

    private final UserService userService;

    private final BookingCodeGenerator bookingCodeGenerator;

    @Override
    public Response createBooking(BookingDTO bookingDTO) {
        User currentUser = userService.getCurrentLoggedInUser();

        Room room = roomRepository.findById(bookingDTO.getRoomId()).orElseThrow(() -> new NotFoundException("Room Not Found"));

        // validation: Ensure the check-in date is not before today
        if(bookingDTO.getCheckInDate().isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today");
        }

        // validation: Ensure the check-out date is not before check-in date
        if(bookingDTO.getCheckInDate().isBefore(bookingDTO.getCheckInDate())){
            throw new InvalidBookingStateAndDateException("check out date cannot be before check in date");
        }

        // validation: Ensure the check-in date is not same as check-out date
        if(bookingDTO.getCheckInDate().isEqual(bookingDTO.getCheckOutDate())){
            throw new InvalidBookingStateAndDateException("check in date cannot be equal to check out date");
        }

        // validate room availability
        boolean isAvalidate = bookingRepository.isRoomAvailable(room.getId(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        if(!isAvalidate){
            throw new InvalidBookingStateAndDateException("Room is not available for the selected date ranges");
        }

        // calculate the total price needed to pay for the stay
        BigDecimal totalPrice = calculateTotalPrice(room, bookingDTO);

        String bookingReference = bookingCodeGenerator.generateBookingReference();

        // create and save the booking
        Booking booking = new Booking();

        booking.setUser(currentUser);

        booking.setRoom(room);

        booking.setCheckInDate(bookingDTO.getCheckInDate());

        booking.setCheckOutDate(bookingDTO.getCheckOutDate());

        booking.setTotalPrice(totalPrice);

        booking.setBookingReference(bookingReference);

        booking.setBookingStatus(BookingStatus.BOOKED);

        booking.setPaymentStatus(PaymentStatus.PENDING);

        booking.setCreatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        // generate the payment url which will be sent via mail
        String paymentUrl = "http://localhost:3000/payment/" + bookingReference + "/" + totalPrice;

        log.info("PAYMENT LINK: {}", paymentUrl);

        // send notification via mail
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(currentUser.getEmail())
                .subject("Booking Confirmation")
                .body(String.format("Your booking has been created successfully. Please proceed with your payment using the payment link below" +
                        "\n\n%s", paymentUrl))
                .bookingReference(bookingReference)
                .build();

        notificationService.sendEmail(notificationDTO); // sending email

        return Response.builder()
                .status(200)
                .message("Booking is successfully")
                .booking(bookingDTO)
                .build();
    }

    @Override
    public Response getAllBookings() {
        List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>() {}.getType());

        for(BookingDTO bookingDTO : bookingDTOList){
            bookingDTO.setUser(null);
            bookingDTO.setRoom(null);
        }

        return Response.builder()
                .status(200)
                .message("success")
                .bookings(bookingDTOList)
                .build();
    }

    @Override
    public Response findBookingByReferenceNo(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking with reference no: " + bookingReference + " Not found"));

        BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
        return Response.builder()
                .status(200)
                .message("success")
                .booking(bookingDTO)
                .build();
    }

    @Override
    public Response updateBooking(BookingDTO bookingDTO) {
        if(bookingDTO.getId() == null) throw new NotFoundException("Booking id is required");

        Booking existingBooking = bookingRepository.findById(bookingDTO.getId())
                .orElseThrow(() -> new NotFoundException("Booking Not found"));

        if(bookingDTO.getCheckInDate() != null){
            existingBooking.setBookingStatus(bookingDTO.getBookingStatus());
        }

        if(bookingDTO.getPaymentStatus() != null){
            existingBooking.setPaymentStatus(bookingDTO.getPaymentStatus());
        }

        bookingRepository.save(existingBooking);

        return Response.builder()
                .status(200)
                .message("Booking update successfully")
                .build();
    }

    private BigDecimal calculateTotalPrice(Room room, BookingDTO bookingDTO) {
        BigDecimal pricePerNight = room.getPricePerNight();
        long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }
}
