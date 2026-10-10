package example.books;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Web層だけを起動するテスト。HTTPの契約(状態、ヘッダー、JSON)を確かめる。serviceは偽物に差し替える
@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService service;

    @Test
    void 登録に成功すると_201とLocationを返す() throws Exception {
        given(service.register(anyString(), anyInt())).willReturn(new BookEntity(7, "こころ", 4, ""));

        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"こころ\",\"copies\":4}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/7"))
                .andExpect(jsonPath("$.title").value("こころ"));

        verify(service).register("こころ", 4);
    }

    @Test
    void 題名が空なら_400で_serviceは呼ばれない() throws Exception {
        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"copies\":4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void 本が見つからない例外は_404になる() throws Exception {
        given(service.find(99)).willThrow(new BookNotFoundException(99));

        mockMvc.perform(get("/api/books/99")).andExpect(status().isNotFound());
    }

    @Test
    void 題名の重複の例外は_409になる() throws Exception {
        given(service.register(anyString(), anyInt())).willThrow(new DuplicateTitleException("こころ"));

        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"こころ\",\"copies\":4}"))
                .andExpect(status().isConflict());
    }
}
