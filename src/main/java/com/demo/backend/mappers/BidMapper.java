package com.demo.backend.mappers;

import com.demo.backend.dto.response.BidDto;
import com.demo.backend.model.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BidMapper {
    @Mapping(target= "taskTitle", source="task.title")
    @Mapping(target= "freelancerEmail", source="freelancer.email")
    BidDto toDto(Bid bid);
}
