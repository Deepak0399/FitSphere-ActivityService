package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityCreateRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.dto.ActivityUpdateRequest;
import com.fitness.activityservice.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityCreateRequest request) {
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> updateActivity(@PathVariable String activityId, @RequestBody ActivityUpdateRequest request){
        log.info("Request to update existing activity: {}", activityId);
        return ResponseEntity.ok(activityService.updateActivity(activityId, request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId) {
        log.info("Request to get user activity for userId: {}", userId);
        return ResponseEntity.ok(activityService.getUserActivity(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }


}
