package com.climbers.hub.gym.contoller;

import com.climbers.hub.gym.dto.GymDto;
import com.climbers.hub.gym.service.GymService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gyms API", description = "클라이밍장 등록, 조회 등")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/gyms")
public class GymController {

    private final GymService gymService;

    @GetMapping
    public ResponseEntity<List<GymDto.GymSimpleResponse>> getAllGym() {
        List<GymDto.GymSimpleResponse> gyms = gymService.getAllGym();
        return ResponseEntity.ok(gyms);
    }

    @Operation(summary = "특정 클라이밍장 조회", description = "gymId를 받아서 특정 클라이밍장을 조회해옵니다.")
    @GetMapping("/{gymId}")
    public ResponseEntity<GymDto.GymDetailResponse> getGym(@PathVariable Long gymId) {
        GymDto.GymDetailResponse gym= gymService.getGym(gymId);
        return ResponseEntity.ok(gym);
    }

    @Operation(summary = "신규 클라이밍장 등록", description = "주소를 포함한 클라이밍장 정보를 받아 등록하고 좌표를 자동 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<Long> createGym(@RequestBody GymDto.GymCreateRequest request) {
        Long gymId = gymService.createGym(request);
        return ResponseEntity.ok(gymId);
    }
}
