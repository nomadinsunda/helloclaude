package com.example.board.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.service.PostService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PostService postService;

    @Test
    void 게시글을_생성하면_201과_Location을_반환한다() {
        LocalDateTime now = LocalDateTime.now();
        given(postService.create(any()))
                .willReturn(new PostResponse(1L, "제목", "내용", "작성자", now, now));

        assertThat(mvc.post().uri("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "제목", "content": "내용", "author": "작성자"}
                        """))
                .hasStatus(HttpStatus.CREATED)
                .headers().hasValue("Location", "http://localhost/api/v1/posts/1");
    }

    @Test
    void 필수값이_없으면_400과_필드_에러를_반환한다() {
        assertThat(mvc.post().uri("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "", "content": "내용", "author": "작성자"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_INPUT_VALUE"))
                .hasPathSatisfying("$.errors[0].field", field -> assertThat(field).isEqualTo("title"));
    }

    @Test
    void 없는_게시글을_조회하면_404_ProblemDetail을_반환한다() {
        given(postService.get(99L)).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        assertThat(mvc.get().uri("/api/v1/posts/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("POST_NOT_FOUND"));
    }

    @Test
    void 허용되지_않은_정렬_항목이면_400을_반환한다() {
        assertThat(mvc.get().uri("/api/v1/posts?sort=content,asc"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_SORT_PROPERTY"));
    }

    @Test
    void 게시글을_삭제하면_204를_반환한다() {
        assertThat(mvc.delete().uri("/api/v1/posts/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void 없는_게시글을_삭제하면_404를_반환한다() {
        willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND)).given(postService).delete(eq(99L));

        assertThat(mvc.delete().uri("/api/v1/posts/99"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
