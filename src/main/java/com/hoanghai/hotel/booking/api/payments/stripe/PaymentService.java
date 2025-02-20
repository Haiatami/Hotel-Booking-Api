package com.hoanghai.hotel.booking.api.payments.stripe;


import com.hoanghai.hotel.booking.api.dtos.NotificationDTO;
import com.hoanghai.hotel.booking.api.entities.Booking;
import com.hoanghai.hotel.booking.api.entities.Payment;
import com.hoanghai.hotel.booking.api.enums.NotificationType;
import com.hoanghai.hotel.booking.api.enums.PaymentGateway;
import com.hoanghai.hotel.booking.api.enums.PaymentStatus;
import com.hoanghai.hotel.booking.api.exceptions.NotFoundException;
import com.hoanghai.hotel.booking.api.payments.stripe.dto.PaymentRequest;
import com.hoanghai.hotel.booking.api.repositories.BookingRepository;
import com.hoanghai.hotel.booking.api.repositories.PaymentRepository;
import com.hoanghai.hotel.booking.api.services.NotificationService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final BookingRepository bookingRepository;

    private final PaymentRepository paymentRepository;

    private final NotificationService notificationService;

    @Value("${stripe.api.secret.key}")
    private String secretKey;

    public String createPaymentIntent(PaymentRequest paymentRequest) {
        log.info("Inside createPaymentIntent()");
        Stripe.apiKey = secretKey;

        String bookingReference = paymentRequest.getBookingReference();

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if(booking.getPaymentStatus() == PaymentStatus.COMPLETED){
            throw new NotFoundException("Payment already made for this booking");
        }

        try{
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) // amount cents
                    .setCurrency("usd")
                    .putMetadata("bookingReference", bookingReference)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return intent.getClientSecret();
        }catch(Exception e){
            throw new RuntimeException("Error creating payment intent");
        }
    }

    public void updatePaymentBooking(PaymentRequest paymentRequest) {
        log.info("Inside updatePaymentBooking()");
        String bookingReference = paymentRequest.getBookingReference();

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        Payment payment = new Payment();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentRequest.getAmount());
        payment.setTransactionId(paymentRequest.getTransactionId());
        payment.setPaymentStatus(paymentRequest.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setBookingReference(bookingReference);
        payment.setUser(booking.getUser());

        if(!paymentRequest.isSuccess()){
            payment.setFailureReason(paymentRequest.getFailureReason());
        }

        paymentRepository.save(payment); // save payment to database

        // create and send notification
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(booking.getUser().getEmail())
                .type(NotificationType.EMAIL)
                .bookingReference(bookingReference)
                .build();

        log.info("About to send notification inside updatePaymentBooking by sms");

        if(paymentRequest.isSuccess()){
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingRepository.save(booking); // Update the booking
            notificationDTO.setSubject("Bookinng Payment Successfully");
            notificationDTO.setBody("Congratulation!! Your payment for booking with reference " + bookingReference + " is successfully");
            notificationService.sendEmail(notificationDTO); // send email
        }else{
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking); // Update the booking
            notificationDTO.setSubject("Bookinng Payment Failed");
            notificationDTO.setBody("Your payment for booking with reference " + bookingReference + " failed with reason: " + paymentRequest.getFailureReason());
            notificationService.sendEmail(notificationDTO); // send email
        }
    }
}
