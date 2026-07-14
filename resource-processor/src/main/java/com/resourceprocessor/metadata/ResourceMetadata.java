package com.resourceprocessor.metadata;

public record ResourceMetadata(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}
