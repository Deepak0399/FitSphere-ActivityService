package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityCreateRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.dto.ActivityUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ActivityService {
    ActivityResponse trackActivity(ActivityCreateRequest request);

    ActivityResponse updateActivity(String activityId, ActivityUpdateRequest request);

    List<ActivityResponse> getUserActivity(String userId);

    ActivityResponse getActivityById(String activityId);
}
