package com.fitness.activityservice.service;

import org.springframework.stereotype.Service;

@Service
public interface UserValidationService {

    boolean validateUser(String userId);
}
