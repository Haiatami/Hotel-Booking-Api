package com.hoanghai.hotel.booking.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HotelBookingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelBookingApiApplication.class, args);
	}
}
