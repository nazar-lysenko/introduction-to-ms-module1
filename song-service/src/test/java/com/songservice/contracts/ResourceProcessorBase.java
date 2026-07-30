package com.songservice.contracts;

import com.songservice.SongServiceApplication;
import com.songservice.song.SongController;
import com.songservice.song.SongService;
import com.songservice.song.dto.SongCreatedDto;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(SongController.class)
@ContextConfiguration(classes = SongServiceApplication.class)
public class ResourceProcessorBase {

    private static final long CREATED_SONG_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SongService songService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        when(songService.createSong(any())).thenReturn(new SongCreatedDto(CREATED_SONG_ID));
    }
}
