package com.climbers.hub.gym.service;

import com.climbers.hub.gym.constant.GymStaffRole;
import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.domain.GymStaff;
import com.climbers.hub.gym.dto.GymOwnerAdminDto;
import com.climbers.hub.gym.repository.GymRepository;
import com.climbers.hub.gym.repository.GymStaffRepository;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GymOwnerAdminService {
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final GymStaffRepository gymStaffRepository;

    @Transactional(readOnly = true)
    public GymOwnerAdminDto.ManagementResponse getManagementData() {
        List<GymOwnerAdminDto.MemberSummary> members = memberRepository.findAll().stream()
                .sorted(Comparator.comparing(Member::getMemberId))
                .map(GymOwnerAdminDto.MemberSummary::from)
                .toList();

        Map<Long, List<GymOwnerAdminDto.OwnerSummary>> ownersByGym = gymStaffRepository.findAll().stream()
                .filter(staff -> staff.getGymRole() == GymStaffRole.OWNER)
                .collect(Collectors.groupingBy(staff -> staff.getGym().getGymId(),
                        Collectors.mapping(GymOwnerAdminDto.OwnerSummary::from, Collectors.toList())));

        List<GymOwnerAdminDto.GymSummary> gyms = gymRepository.findAll().stream()
                .sorted(Comparator.comparing(Gym::getName).thenComparing(Gym::getGymId))
                .map(gym -> GymOwnerAdminDto.GymSummary.from(gym,
                        ownersByGym.getOrDefault(gym.getGymId(), List.of()).stream()
                                .sorted(Comparator.comparing(GymOwnerAdminDto.OwnerSummary::name)
                                        .thenComparing(GymOwnerAdminDto.OwnerSummary::memberId))
                                .toList()))
                .toList();

        return new GymOwnerAdminDto.ManagementResponse(members, gyms);
    }

    @Transactional
    public GymOwnerAdminDto.OwnerSummary assignOwner(Long gymId, Long memberId) {
        if (gymStaffRepository.existsByMember_MemberIdAndGym_GymIdAndGymRole(
                memberId, gymId, GymStaffRole.OWNER)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 이 암장의 관리인으로 지정된 회원입니다.");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "암장을 찾을 수 없습니다."));
        return GymOwnerAdminDto.OwnerSummary.from(gymStaffRepository.save(GymStaff.owner(member, gym)));
    }

    @Transactional
    public GymOwnerAdminDto.OwnerSummary replaceOwner(Long gymId, Long memberId, Long newMemberId) {
        GymStaff currentStaff = gymStaffRepository.findByMember_MemberIdAndGym_GymIdAndGymRole(
                        memberId, gymId, GymStaffRole.OWNER)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 암장 관리인 지정을 찾을 수 없습니다."));
        if (memberId.equals(newMemberId)) {
            return GymOwnerAdminDto.OwnerSummary.from(currentStaff);
        }
        if (gymStaffRepository.existsByMember_MemberIdAndGym_GymIdAndGymRole(
                newMemberId, gymId, GymStaffRole.OWNER)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 이 암장의 관리인으로 지정된 회원입니다.");
        }

        Member newMember = memberRepository.findById(newMemberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        Gym gym = currentStaff.getGym();
        gymStaffRepository.delete(currentStaff);
        return GymOwnerAdminDto.OwnerSummary.from(gymStaffRepository.save(GymStaff.owner(newMember, gym)));
    }

    @Transactional
    public void removeOwner(Long gymId, Long memberId) {
        GymStaff staff = gymStaffRepository.findByMember_MemberIdAndGym_GymIdAndGymRole(
                        memberId, gymId, GymStaffRole.OWNER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 암장 관리인 지정을 찾을 수 없습니다."));
        gymStaffRepository.delete(staff);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long memberId, Long gymId) {
        return gymStaffRepository.existsByMember_MemberIdAndGym_GymIdAndGymRole(
                memberId, gymId, GymStaffRole.OWNER);
    }
}
