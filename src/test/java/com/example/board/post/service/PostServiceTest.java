package com.example.board.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostUpdateRequest;
import com.example.board.post.repository.PostRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void 게시글을_생성한다() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        PostResponse response = postService.create(new PostCreateRequest("제목", "내용", "작성자"));

        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.content()).isEqualTo("내용");
        assertThat(response.author()).isEqualTo("작성자");
    }

    @Test
    void 없는_게시글을_조회하면_예외가_발생한다() {
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.get(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void 게시글을_수정한다() {
        Post post = Post.create("제목", "내용", "작성자");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        PostResponse response = postService.update(1L, new PostUpdateRequest("새 제목", "새 내용"));

        assertThat(response.title()).isEqualTo("새 제목");
        assertThat(response.content()).isEqualTo("새 내용");
        assertThat(response.author()).isEqualTo("작성자");
    }

    @Test
    void 없는_게시글은_삭제하지_않고_예외가_발생한다() {
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(1L))
                .isInstanceOf(BusinessException.class);
        then(postRepository).should(never()).delete(any());
    }
}
