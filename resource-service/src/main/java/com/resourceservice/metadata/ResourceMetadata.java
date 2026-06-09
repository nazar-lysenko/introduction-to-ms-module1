package com.resourceservice.metadata;

public record ResourceMetadata(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}
