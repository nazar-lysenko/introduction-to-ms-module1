package com.songservice.song.dto;

public record SongResponseDto(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year
) {}
