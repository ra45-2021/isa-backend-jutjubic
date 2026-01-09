package com.jutjubic.controller;

import com.jutjubic.domain.User;
import com.jutjubic.dto.UserDto;
import com.jutjubic.repository.UserRepository;
import com.jutjubic.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;
    private final CurrentUserService currentUser;

    public UserController(UserRepository users, CurrentUserService currentUser) {
        this.users = users;
        this.currentUser = currentUser;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getByUsername(@PathVariable String username) {
        return users.findByUsername(username)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        User u = currentUser.require();
        return ResponseEntity.ok(toDto(u));
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
