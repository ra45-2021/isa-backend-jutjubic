package com.jutjubic.controller;

import com.jutjubic.domain.User;
import com.jutjubic.dto.UserDto;
import com.jutjubic.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getByUsername(@PathVariable String username) {
        return users.findByUsername(username)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                u.getEmailAdress(),
                u.getName(),
                u.getSurname(),
                u.getAdress(),
                u.getBio(),
                u.getRole(),
                u.getProfileImageUrl()
        );
    }
}
