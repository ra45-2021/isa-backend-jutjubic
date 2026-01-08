package com.jutjubic.service;

import com.jutjubic.domain.User;
import com.jutjubic.dto.LoginDto;
import com.jutjubic.dto.RegisterDto;
import com.jutjubic.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.jutjubic.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAttemptTime = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    public User register(RegisterDto dto) {
        if (userRepository.findByEmailAdress(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Korisnik sa tim email-om već postoji!");
        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Korisničko ime je zauzeto!");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmailAdress(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setAdress(dto.getAdress());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole("USER");
        user.setActive(false);
        user.setActivationToken(UUID.randomUUID().toString());

        User savedUser = userRepository.save(user);

        emailService.sendActivationEmail(savedUser.getEmailAdress(), savedUser.getActivationToken());

        return savedUser;
    }

    public void activateUser(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Nevalidan aktivacioni token!"));

        user.setActive(true);
        user.setActivationToken(null); // Brišemo token jer je upotrebljen
        userRepository.save(user);
    }

    public String login(LoginDto dto, String ip) {
        if (isIpBlocked(ip)) {
            throw new RuntimeException("Previše neuspelih pokušaja. Pokušajte ponovo za minut.");
        }

        try {
            User user = userRepository.findByEmailAdress(dto.getEmailAdress())
                    .orElseThrow(() -> new RuntimeException("Pogrešno korisničko ime ili lozinka!"));

            if (!user.isActive()) {
                throw new RuntimeException("Nalog nije aktiviran. Proverite email.");
            }

            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new RuntimeException("Pogrešno korisničko ime ili lozinka!");
            }

            // Ako je login uspešan, resetuj brojač za tu IP adresu
            attemptsCache.remove(ip);
            return jwtUtil.generateToken(user.getEmailAdress());

        } catch (RuntimeException e) {
            // Ako je login neuspešan, povećaj brojač
            registerFailedAttempt(ip);
            throw e;
        }
    }

    private void registerFailedAttempt(String ip) {
        int attempts = attemptsCache.getOrDefault(ip, 0);
        attemptsCache.put(ip, attempts + 1);
        lastAttemptTime.put(ip, LocalDateTime.now());
    }

    private boolean isIpBlocked(String ip) {
        if (!attemptsCache.containsKey(ip)) return false;

        int attempts = attemptsCache.get(ip);
        LocalDateTime lastTime = lastAttemptTime.get(ip);

        // Ako je prošlo više od 1 minuta, resetuj blokadu
        if (lastTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
            attemptsCache.remove(ip);
            return false;
        }

        return attempts >= 5;
    }
}