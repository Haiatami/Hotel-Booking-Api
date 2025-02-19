package com.hoanghai.hotel.booking.api.services;

import com.hoanghai.hotel.booking.api.dtos.LoginRequest;
import com.hoanghai.hotel.booking.api.dtos.RegistrationRequest;
import com.hoanghai.hotel.booking.api.dtos.Response;
import com.hoanghai.hotel.booking.api.dtos.UserDTO;
import com.hoanghai.hotel.booking.api.entities.User;

public interface UserService {
    Response registerUser(RegistrationRequest registrationRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    Response getOwnAccountDetails();

    User getCurrentLoggedInUser();

    Response updateOwnAccount(UserDTO userDTO);

    Response deleteOwnAccount();

    Response getMyBookingHistory();

}
