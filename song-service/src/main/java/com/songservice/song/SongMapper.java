package com.songservice.song;

import com.songservice.song.dto.SongRequestDto;
import com.songservice.song.dto.SongCreatedDto;
import com.songservice.song.dto.SongResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SongMapper {
    @Mapping(target = "id", source = "id")
    Song toSong(SongRequestDto requestDto);
    SongCreatedDto toCreatedDto(Song song);
    SongResponseDto toResponseDto(Song song);
}
