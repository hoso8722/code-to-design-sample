package example.books;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Web層だけを起動するテスト。HTTPの契約(状態、ヘッダー、JSON)と、認証と認可を確かめる。serviceは偽物に差し替える
@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class BookControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService service;

    @Test
    void 認証なしの一覧の取得は_401になる() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void readerは一覧を取得できる() throws Exception {
        given(service.list("id")).willReturn(List.of(new BookEntity(1, "吾輩は猫である", 3)));

        mockMvc.perform(get("/api/books").with(httpBasic("reader", "reader-test-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("吾輩は猫である"));
    }

    @Test
    void readerが登録しようとすると_403になる() throws Exception {
        mockMvc.perform(post("/api/books").with(httpBasic("reader", "reader-test-pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"こころ\",\"copies\":4}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://api.example.com/problems/forbidden"));
    }

    @Test
    void adminが登録すると_201とLocationを返す() throws Exception {
        given(service.register(anyString(), anyInt())).willReturn(new BookEntity(7, "こころ", 4));

        mockMvc.perform(post("/api/books").with(httpBasic("admin", "admin-test-pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"こころ\",\"copies\":4}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/7"));
    }

    @Test
    void 題名が空なら_400で_errorsに項目が入る() throws Exception {
        mockMvc.perform(post("/api/books").with(httpBasic("admin", "admin-test-pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"copies\":4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.example.com/problems/validation-error"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void 本が見つからない例外は_404のProblemDetailになる() throws Exception {
        given(service.find(99)).willThrow(new BookNotFoundException(99));

        mockMvc.perform(get("/api/books/99").with(httpBasic("reader", "reader-test-pass")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.example.com/problems/book-not-found"));
    }
}
