package com.songservice.song;

import com.songservice.song.dto.SongCreatedDto;
import com.songservice.song.dto.SongDeletedDto;
import com.songservice.song.dto.SongRequestDto;
import com.songservice.song.dto.SongResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {
    private static final String CSV_ID_SEPARATOR = ",";

    private final SongRepository songRepository;
    private final SongMapper mapper;

    public SongCreatedDto createSong(SongRequestDto songRequestDto) {
        if (songRepository.existsById(songRequestDto.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Metadata for resource ID=%d already exists".formatted(songRequestDto.id()));
        }

        Song song = mapper.toSong(songRequestDto);
        song = songRepository.save(song);

        return mapper.toCreatedDto(song);
    }

    public SongResponseDto getSong(Long id) {
        return songRepository.findById(id)
                .map(mapper::toResponseDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song metadata for ID=%d not found".formatted(id)));
    }

    public SongDeletedDto deleteSongs(String idsParam) {
        List<Long> ids = Arrays.stream(idsParam.split(CSV_ID_SEPARATOR))
                .map(Long::parseLong)
                .toList();

        List<Song> existingResources = songRepository.findAllById(ids);
        List<Long> idsToDelete = existingResources.stream()
                .map(Song::getId)
                .toList();
        songRepository.deleteAllById(idsToDelete);

        return new SongDeletedDto(idsToDelete);
    }
}
