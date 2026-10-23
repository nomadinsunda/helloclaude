package com.example.board.post.service;

import com.example.board.global.common.PageResponse;
import com.example.board.global.error.BusinessException;
import com.example.board.global.error.ErrorCode;
import com.example.board.post.domain.Post;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostSummaryResponse;
import com.example.board.post.dto.PostUpdateRequest;
import com.example.board.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public PostResponse create(PostCreateRequest request) {
        Post post = Post.create(request.title(), request.content(), request.author());
        return PostResponse.from(postRepository.save(post));
    }

    public PostResponse get(Long postId) {
        return PostResponse.from(findPost(postId));
    }

    public PageResponse<PostSummaryResponse> getList(Pageable pageable) {
        return PageResponse.from(postRepository.findAll(pageable).map(PostSummaryResponse::from));
    }

    @Transactional
    public PostResponse update(Long postId, PostUpdateRequest request) {
        Post post = findPost(postId);
        post.update(request.title(), request.content());
        // updatedAt은 flush 시점에 Auditing으로 갱신되므로, 응답에 반영하기 위해 먼저 flush한다.
        postRepository.flush();
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long postId) {
        postRepository.delete(findPost(postId));
    }

    /**
     * 다른 도메인(댓글 등)에서 게시글 엔티티가 필요할 때 사용한다. 없으면 {@link ErrorCode#POST_NOT_FOUND}.
     */
    public Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
