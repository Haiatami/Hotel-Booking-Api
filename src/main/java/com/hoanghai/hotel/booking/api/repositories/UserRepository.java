package com.hoanghai.hotel.booking.api.repositories;

import com.hoanghai.hotel.booking.api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
