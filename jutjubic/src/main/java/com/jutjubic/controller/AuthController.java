package com.jutjubic.controller;

import com.jutjubic.dto.LoginDto;
import com.jutjubic.dto.RegisterDto;
import com.jutjubic.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto) {
        try {
            authService.register(registerDto);
            return ResponseEntity.ok("Registracija uspešna! Proverite vaš email za aktivaciju.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, HttpServletRequest request) {
        try {
            String ip = request.getRemoteAddr();
            String token = authService.login(loginDto, ip);
            return ResponseEntity.ok().body("{\"token\": \"" + token + "\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        try {
            authService.activateUser(token);
            return ResponseEntity.ok("Nalog je uspešno aktiviran! Sada možete da se prijavite.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}