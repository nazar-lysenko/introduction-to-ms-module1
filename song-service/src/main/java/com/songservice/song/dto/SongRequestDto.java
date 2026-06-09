package com.songservice.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SongRequestDto(
        @NotNull
        Long id,

        @NotNull(message = "Song name is required")
        @NotBlank(message = "Song name must be between 1 and 100 characters")
        @Size(max = 100, message = "Song name must be between 1 and 100 characters")
        String name,

        @NotNull(message = "Artist name is required")
        @NotBlank(message = "Artist name must be between 1 and 100 characters")
        @Size(max = 100, message = "Artist name must be between 1 and 100 characters")
        String artist,

        @NotNull(message = "Album name is required")
        @NotBlank(message = "Album name must be between 1 and 100 characters")
        @Size(max = 100, message = "Album name must be between 1 and 100 characters")
        String album,

        @NotNull(message = "Duration is required")
        @NotBlank(message = "Duration must be in mm:ss format with leading zeros")
        @Pattern(regexp = "^[0-9]{2}:[0-5][0-9]$", message = "Duration must be in mm:ss format with leading zeros")
        String duration,

        @NotNull(message = "Year is required")
        @NotBlank(message = "Year must be between 1900 and 2099")
        @Pattern(regexp = "^(19|20)[0-9]{2}$", message = "Year must be between 1900 and 2099")
        String year
) {}
