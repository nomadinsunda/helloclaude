package com.example.board.post.controller;

import com.example.board.global.common.PageResponse;
import com.example.board.global.common.SortValidator;
import com.example.board.post.dto.PostCreateRequest;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.PostSummaryResponse;
import com.example.board.post.dto.PostUpdateRequest;
import com.example.board.post.service.PostService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "createdAt", "updatedAt");

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest request) {
        PostResponse response = postService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{postId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PageResponse<PostSummaryResponse> getList(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        SortValidator.validate(pageable.getSort(), SORTABLE_PROPERTIES);
        return postService.getList(pageable);
    }

    @GetMapping("/{postId}")
    public PostResponse get(@PathVariable Long postId) {
        return postService.get(postId);
    }

    @PutMapping("/{postId}")
    public PostResponse update(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequest request) {
        return postService.update(postId, request);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId) {
        postService.delete(postId);
        return ResponseEntity.noContent().build();
    }
}
