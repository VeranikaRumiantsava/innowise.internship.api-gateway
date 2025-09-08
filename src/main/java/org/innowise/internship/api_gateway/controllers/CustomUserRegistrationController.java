package org.innowise.internship.api_gateway.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.internship.api_gateway.dto.UserRegistrationDTO;
import org.innowise.internship.api_gateway.services.CustomUserRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
public class CustomUserRegistrationController {

    private final CustomUserRegistrationService customUserRegistrationService;

    @PostMapping
    public ResponseEntity<Mono<Void>> registerUser(@Valid UserRegistrationDTO userRegistrationDTO) {
        return ResponseEntity.ok(
                customUserRegistrationService.register(userRegistrationDTO)
        );
    }
}
