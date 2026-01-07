package com.jutjubic.repository;

import com.jutjubic.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Potrebno za proveru duplikata prilikom registracije
    Optional<User> findByEmailAdress(String emailAdress);

    // Potrebno za pronalaženje korisnika kada klikne na link u mejlu
    Optional<User> findByActivationToken(String activationToken);
}