package com.example.board.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.board.comment.domain.Comment;
import com.example.board.comment.dto.CommentCreateRequest;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CommentUpdateRequest;
import com.example.board.comment.repository.CommentRepository;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.service.PostService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostService postService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void 댓글을_생성한다() {
        Post post = Post.create("제목", "내용", "작성자");
        given(postService.findPost(1L)).willReturn(post);
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = commentService.create(1L, new CommentCreateRequest("댓글", "댓글 작성자"));

        assertThat(response.content()).isEqualTo("댓글");
        assertThat(response.author()).isEqualTo("댓글 작성자");
    }

    @Test
    void 없는_게시글에는_댓글을_생성할_수_없다() {
        given(postService.findPost(1L)).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        assertThatThrownBy(() -> commentService.create(1L, new CommentCreateRequest("댓글", "작성자")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
        then(commentRepository).should(never()).save(any());
    }

    @Test
    void 없는_게시글의_댓글_목록을_조회하면_예외가_발생한다() {
        given(postService.findPost(1L)).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));

        assertThatThrownBy(() -> commentService.getList(1L, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void 댓글을_수정한다() {
        Comment comment = Comment.create(Post.create("제목", "내용", "작성자"), "댓글", "작성자");
        given(commentRepository.findByIdAndPostId(10L, 1L)).willReturn(Optional.of(comment));

        CommentResponse response = commentService.update(1L, 10L, new CommentUpdateRequest("수정된 댓글"));

        assertThat(response.content()).isEqualTo("수정된 댓글");
    }

    @Test
    void 해당_게시글의_댓글이_아니면_삭제하지_않고_예외가_발생한다() {
        given(commentRepository.findByIdAndPostId(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        then(commentRepository).should(never()).delete(any());
    }
}
