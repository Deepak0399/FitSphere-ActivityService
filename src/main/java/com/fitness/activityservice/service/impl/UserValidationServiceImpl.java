package com.fitness.activityservice.service.impl;

import com.fitness.activityservice.exception.InvalidRequestException;
import com.fitness.activityservice.exception.ResourceNotFoundException;
import com.fitness.activityservice.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserValidationServiceImpl implements UserValidationService {

    private final WebClient userServiceWebClient;

    @Override
    public boolean validateUser(String userId) {
        log.info("Calling User Validation API for UserId: {}", userId);
        try {
            return Boolean.TRUE.equals(userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
        } catch (WebClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND)
                throw new ResourceNotFoundException("User Not Found: " + userId);
            else if (exception.getStatusCode() == HttpStatus.BAD_REQUEST)
                throw new InvalidRequestException("Invalid Request: " + userId);
        }
        return false;
    }
}
