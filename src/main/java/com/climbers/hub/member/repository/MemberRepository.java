package com.climbers.hub.member.repository;

import com.climbers.hub.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
