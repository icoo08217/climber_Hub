package com.climbers.hub.mapper;

import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.dto.MemberDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-12T11:17:15+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.9 (Oracle Corporation)"
)
@Component
public class MemberMapperImpl implements MemberMapper {

    @Override
    public void updateFromDto(Member member, MemberDto.MemberUpdateRequest dto) {
        if ( dto == null ) {
            return;
        }
    }
}
