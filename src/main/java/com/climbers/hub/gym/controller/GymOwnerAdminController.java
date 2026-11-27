package com.climbers.hub.gym.controller;

import com.climbers.hub.gym.dto.GymOwnerAdminDto;
import com.climbers.hub.gym.service.GymOwnerAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class GymOwnerAdminController {
    private final GymOwnerAdminService gymOwnerAdminService;

    @GetMapping("/gym-owners")
    public GymOwnerAdminDto.ManagementResponse getManagementData() {
        return gymOwnerAdminService.getManagementData();
    }

    @PostMapping("/gyms/{gymId}/owners")
    public ResponseEntity<GymOwnerAdminDto.OwnerSummary> assignOwner(
            @PathVariable Long gymId, @Valid @RequestBody GymOwnerAdminDto.AssignOwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gymOwnerAdminService.assignOwner(gymId, request.memberId()));
    }

    @PatchMapping("/gyms/{gymId}/owners/{memberId}")
    public GymOwnerAdminDto.OwnerSummary replaceOwner(
            @PathVariable Long gymId,
            @PathVariable Long memberId,
            @Valid @RequestBody GymOwnerAdminDto.ReplaceOwnerRequest request) {
        return gymOwnerAdminService.replaceOwner(gymId, memberId, request.newMemberId());
    }

    @DeleteMapping("/gyms/{gymId}/owners/{memberId}")
    public ResponseEntity<Void> removeOwner(@PathVariable Long gymId, @PathVariable Long memberId) {
        gymOwnerAdminService.removeOwner(gymId, memberId);
        return ResponseEntity.noContent().build();
    }
}
