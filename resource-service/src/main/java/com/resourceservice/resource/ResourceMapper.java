package com.resourceservice.resource;

import com.resourceservice.resource.dto.ResourceCreatedDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceMapper {
    ResourceCreatedDto toCreatedDto(Resource resource);
}
