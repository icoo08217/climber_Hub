package com.climbers.hub.gym.contoller;

import com.climbers.hub.gym.dto.GymDto;
import com.climbers.hub.gym.service.GymService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gyms API", description = "클라이밍장 등록, 조회 등")
@RestController
@RequiredArgsConstructor
@RequestMapping("/gyms")
public class GymController {

    private final GymService gymService;

    @Operation(summary = "신규 클라이밍장 등록", description = "주소를 포함한 클라이밍장 정보를 받아 등록하고 좌표를 자동 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<Long> createGym(@RequestBody GymDto.GymCreateRequest request) {
        Long gymId = gymService.createGym(request);
        return ResponseEntity.ok(gymId);
    }
}
