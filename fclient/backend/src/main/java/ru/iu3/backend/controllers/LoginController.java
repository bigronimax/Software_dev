package ru.iu3.backend.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.iu3.backend.models.User;
import ru.iu3.backend.repositories.UserRepository;
import ru.iu3.backend.tools.Utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> credentials) {
        String login = credentials.get("login");
        String pwd = credentials.get("password");
        String hash = Utils.ComputeHash(pwd, "00");
        System.out.println(hash);
        if (pwd.isEmpty() || login.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid login or password");
        } else {
            Optional<User> optionalUser = userRepository.findByLogin(login);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                String hashedPassword = user.password;
                String salt = user.salt;
                String computedHash = Utils.ComputeHash(pwd, salt);
                System.out.println(computedHash);
                if (hashedPassword.equalsIgnoreCase(computedHash)) {
                    String token = UUID.randomUUID().toString();
                    user.token = token;
                    user.activity = LocalDateTime.now();
                    User savedUser = userRepository.saveAndFlush(user);
                    return new ResponseEntity<Object>(savedUser, HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<Object>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && !token.isEmpty()) {
            token = StringUtils.removeStart(token, "Bearer").trim();
            Optional<User> optionalUser = userRepository.findByToken(token);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.token = null;
                userRepository.save(user);
                return new ResponseEntity<String>(HttpStatus.OK);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}