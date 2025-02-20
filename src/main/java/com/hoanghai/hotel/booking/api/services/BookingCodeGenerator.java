package com.hoanghai.hotel.booking.api.services;

import com.hoanghai.hotel.booking.api.entities.BookingReference;
import com.hoanghai.hotel.booking.api.repositories.BookingReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class BookingCodeGenerator {
    private final BookingReferenceRepository bookingReferenceRepository;

    public String generateBookingReference() {
        String bookingReference;

        // keep generating until a unique code is found
        do{
            bookingReference = generateRandomAlphaNumericCode(10); // generate code of length 10
        }while (isBookingReferenceExist(bookingReference)); // check if the code already exist. If it doesn't, exit

        saveBookingReferenceToDatabase(bookingReference); // save the code to database

        return bookingReference;
    }

    private String generateRandomAlphaNumericCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";

        Random random = new Random();

        StringBuffer stringBuffer = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());

            stringBuffer.append(characters.charAt(index));
        }

        return stringBuffer.toString();
    }

    private boolean isBookingReferenceExist(String bookingReference){
        return bookingReferenceRepository.findByReferenceNo(bookingReference).isPresent();
    }

    private void saveBookingReferenceToDatabase(String bookingReference){
        BookingReference newBookingReference = BookingReference.builder().referenceNo(bookingReference).build();

        bookingReferenceRepository.save(newBookingReference);
    }
}
