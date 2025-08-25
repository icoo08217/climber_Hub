package com.climbers.hub.mapper;

import com.climbers.hub.member.domain.Member;
import com.climbers.hub.member.dto.MemberDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    /**
     * MemberUpdateRequest DTO의 null이 아닌 필드만 Member Entity에 업데이트 합니다.
     * @param member - DB에서 조회한, 업데이트될 대상 Entity
     * @param dto - 업데이트할 데이터가 담긴 DTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(@MappingTarget Member member, MemberDto.MemberUpdateRequest dto);
}
