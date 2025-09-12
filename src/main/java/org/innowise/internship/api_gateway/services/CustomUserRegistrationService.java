package org.innowise.internship.api_gateway.services;

import lombok.RequiredArgsConstructor;
import org.innowise.internship.api_gateway.dto.UserRegistrationDTO;
import org.innowise.internship.api_gateway.dto.auth.RegisterRequestDTO;
import org.innowise.internship.api_gateway.dto.user.UserCreateDTO;
import org.innowise.internship.api_gateway.dto.user.UserResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CustomUserRegistrationService {

    private final WebClient userClient;
    private final WebClient authClient;

    public CustomUserRegistrationService(WebClient.Builder webClientBuilder) {
        this.userClient = webClientBuilder.baseUrl("http://userservice:8080").build();
        this.authClient = webClientBuilder.baseUrl("http://authservice:8080").build();
    }

    public Mono<Void> register(UserRegistrationDTO userRegistrationDTO) {
        return createUser(userRegistrationDTO)
                .flatMap(user -> createAuth(userRegistrationDTO, user.getId())
                        .onErrorResume(e -> rollbackUser(user.getId(), e))
                );
    }

    private Mono<UserResponseDTO> createUser(UserRegistrationDTO userRegistrationDTO) {
        return userClient.post()
                .uri("/user")
                .bodyValue(new UserCreateDTO(
                        userRegistrationDTO.getName(),
                        userRegistrationDTO.getSurname(),
                        userRegistrationDTO.getBirthDate(),
                        userRegistrationDTO.getEmail()
                ))
                .retrieve()
                .bodyToMono(UserResponseDTO.class);
    }

    private Mono<Void> createAuth(UserRegistrationDTO dto, Long userId) {
        RegisterRequestDTO authDTO = new RegisterRequestDTO(
                dto.getLogin(),
                dto.getPassword(),
                userId
        );

        // Логируем объект перед отправкой
        System.out.println("Sending to authservice: " + authDTO);


        return authClient.post()
                .uri("/api/v1/auth/register")
                .bodyValue(authDTO)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Mono<Void> rollbackUser(Long userId, Throwable error) {
        return userClient.delete()
                .uri("/user/{id}", userId)
                .retrieve()
                .bodyToMono(Void.class)
                .then(Mono.error(error));
    }
}

