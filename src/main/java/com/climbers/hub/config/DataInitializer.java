package com.climbers.hub.config;


import com.climbers.hub.gym.domain.Gym;
import com.climbers.hub.gym.repository.GymRepository;
import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Init Data Insert Start");

        // Member 데이터 생성
        Member member1 = Member.builder()
                .name("전병찬")
                .email("testtest@naver.com")
                .password("1234")
                .build();
        memberRepository.save(member1);

        Member member2 = Member.builder()
                .name("홍길동")
                .email("test@gmail.com")
                .password("0000")
                .build();
        memberRepository.save(member2);

        // Gym 데이터 생성
        Gym gym1 = Gym.builder()
                .name("더클라임 신림점")
                .description("신림역 더클라임 암장")
                .address("서울 관악구 신림로 340")
                .phone("02-0000-0000")
                .openHours("평일 10:00 ~ 23:00 / 주말 10:00 ~ 20:00")
                .pricingInfo("일일 이용권 22,000원")
                .latitude(37.4847794)
                .longitude(126.930118)
                .build();

        gymRepository.save(gym1);

        log.info("Init Data Insert Success!!");
    }
}
