package com.songservice.song;

import com.songservice.song.dto.SongCreatedDto;
import com.songservice.song.dto.SongDeletedDto;
import com.songservice.song.dto.SongRequestDto;
import com.songservice.song.dto.SongResponseDto;
import com.songservice.song.validation.ValidCsvIds;
import com.songservice.song.validation.ValidSongId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/songs")
@Validated
public class SongController {
    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongCreatedDto> createSong(@RequestBody @Valid SongRequestDto requestDto) {
        return ResponseEntity.ok(songService.createSong(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponseDto> getSong(@PathVariable @ValidSongId Long id) {
        return ResponseEntity.ok(songService.getSong(id));
    }

    @DeleteMapping
    public ResponseEntity<SongDeletedDto> deleteSong(@RequestParam @ValidCsvIds String id) {
        return ResponseEntity.ok(songService.deleteSongs(id));
    }
}
