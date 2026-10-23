package com.example.board.comment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.service.CommentService;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void 댓글을_생성하면_201과_Location을_반환한다() {
        LocalDateTime now = LocalDateTime.now();
        given(commentService.create(eq(1L), any()))
                .willReturn(new CommentResponse(10L, 1L, "댓글", "작성자", now, now));

        assertThat(mvc.post().uri("/api/v1/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content": "댓글", "author": "작성자"}
                        """))
                .hasStatus(HttpStatus.CREATED)
                .headers().hasValue("Location", "http://localhost/api/v1/posts/1/comments/10");
    }

    @Test
    void 내용이_없으면_400과_필드_에러를_반환한다() {
        assertThat(mvc.post().uri("/api/v1/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content": " ", "author": "작성자"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_INPUT_VALUE"))
                .hasPathSatisfying("$.errors[0].field", field -> assertThat(field).isEqualTo("content"));
    }

    @Test
    void 없는_게시글에_댓글을_달면_404를_반환한다() {
        given(commentService.create(eq(99L), any())).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        assertThat(mvc.post().uri("/api/v1/posts/99/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content": "댓글", "author": "작성자"}
                        """))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("POST_NOT_FOUND"));
    }

    @Test
    void 없는_댓글을_조회하면_404를_반환한다() {
        given(commentService.get(1L, 99L)).willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        assertThat(mvc.get().uri("/api/v1/posts/1/comments/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("COMMENT_NOT_FOUND"));
    }

    @Test
    void 허용되지_않은_정렬_항목이면_400을_반환한다() {
        assertThat(mvc.get().uri("/api/v1/posts/1/comments?sort=author,asc"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.code", code -> assertThat(code).isEqualTo("INVALID_SORT_PROPERTY"));
    }

    @Test
    void 댓글을_삭제하면_204를_반환한다() {
        assertThat(mvc.delete().uri("/api/v1/posts/1/comments/10"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }
}
