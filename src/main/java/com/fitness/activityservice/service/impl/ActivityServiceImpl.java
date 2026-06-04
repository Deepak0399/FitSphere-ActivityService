package com.fitness.activityservice.service.impl;

import com.fitness.activityservice.dto.ActivityCreateRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.dto.ActivityUpdateRequest;
import com.fitness.activityservice.exception.InvalidRequestException;
import com.fitness.activityservice.exception.ResourceNotFoundException;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repositories.ActivityRepository;
import com.fitness.activityservice.service.ActivityService;
import com.fitness.activityservice.service.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class  ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Override
    public ActivityResponse trackActivity(ActivityCreateRequest request) {

        log.info("Registering activity from service for object: {}",request);
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if (!isValidUser) {
            throw new InvalidRequestException("Invalid User: " + request.getUserId());
        }

        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
        Activity savedActivity = activityRepository.save(activity);
        log.info("Activity saved: {}", savedActivity);

        // --> Publish to RabbitMQ for AI processing...
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
        } catch (MessageConversionException e) {
            log.error("Failed to Convert the message data: ", e);
        } catch (AmqpConnectException e) {
        // Case: RabbitMQ broker is down or network-unreachable
        log.error("Cannot connect to RabbitMQ broker.", e);
        } catch (AmqpException e) {
        // Case: General AMQP errors (e.g., channel closed, admin errors)
        log.error("AMQP error occurred during send.", e);
        } catch (Exception e) {
        // Case: Unexpected runtime exceptions
        log.error("Unexpected error during message sending.", e);
    }
        log.info("Activity send to queue for recommendation processing: {}", savedActivity);
        return mapToResponse(savedActivity);
    }

    @Override
    public ActivityResponse updateActivity(String activityId, ActivityUpdateRequest request) {
        //check weather activity is available or not using activityId
        Activity activity = activityRepository.findById(activityId).orElseThrow(
                () -> new ResourceNotFoundException("Activity not found with id: " + activityId));
        activity.setType(request.getType());
        activity.setDuration(request.getDuration());
        activity.setCaloriesBurned(request.getCaloriesBurned());
        activity.setStartTime(request.getStartTime());
        activity.setAdditionalMetrics(request.getAdditionalMetrics());
        Activity savedActivity = activityRepository.save(activity);
        log.info("Updated Activity saved: {}", savedActivity);
        return mapToResponse(savedActivity);
    }
    @Override
    public List<ActivityResponse> getUserActivity(String userId) {

        boolean isValidUser = userValidationService.validateUser(userId);
        if (!isValidUser) {
            throw new InvalidRequestException("Invalid User: " + userId);
        }
        List<Activity> activities = activityRepository.findByUserId(userId);
        List<ActivityResponse> userActivity = activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("User activity for userId - {} - {}",userId,userActivity);
        return userActivity;
    }

    @Override
    public ActivityResponse getActivityById(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: "+ activityId));
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }
}
