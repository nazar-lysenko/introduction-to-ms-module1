package com.songservice.contracts;

import com.songservice.SongServiceApplication;
import com.songservice.song.SongController;
import com.songservice.song.SongService;
import com.songservice.song.dto.SongDeletedDto;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(SongController.class)
@ContextConfiguration(classes = SongServiceApplication.class)
public class ResourceServiceBase {

    private static final String DELETE_IDS_PARAM = "1,2";
    private static final List<Long> DELETED_IDS = List.of(1L, 2L);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SongService songService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        when(songService.deleteSongs(DELETE_IDS_PARAM)).thenReturn(new SongDeletedDto(DELETED_IDS));
    }
}
