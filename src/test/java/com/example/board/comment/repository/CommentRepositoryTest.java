package com.example.board.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.board.comment.domain.Comment;
import com.example.board.global.config.JpaAuditingConfig;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import com.example.board.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    void 게시글별로_댓글을_페이징_조회한다() {
        Post post = postRepository.save(Post.create("제목", "내용", "작성자"));
        Post otherPost = postRepository.save(Post.create("다른 글", "내용", "작성자"));
        commentRepository.save(Comment.create(post, "댓글1", "작성자"));
        commentRepository.save(Comment.create(post, "댓글2", "작성자"));
        commentRepository.save(Comment.create(otherPost, "다른 글 댓글", "작성자"));

        Page<Comment> page = commentRepository.findByPostId(post.getId(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Comment::getContent).containsExactly("댓글1", "댓글2");
    }

    @Test
    void 다른_게시글의_댓글은_조회되지_않는다() {
        Post post = postRepository.save(Post.create("제목", "내용", "작성자"));
        Post otherPost = postRepository.save(Post.create("다른 글", "내용", "작성자"));
        Comment comment = commentRepository.save(Comment.create(post, "댓글", "작성자"));

        assertThat(commentRepository.findByIdAndPostId(comment.getId(), post.getId())).isPresent();
        assertThat(commentRepository.findByIdAndPostId(comment.getId(), otherPost.getId())).isEmpty();
    }

    @Test
    void 게시글을_삭제하면_댓글도_함께_삭제된다() {
        Post post = postRepository.save(Post.create("제목", "내용", "작성자"));
        commentRepository.save(Comment.create(post, "댓글", "작성자"));
        commentRepository.flush();

        postRepository.delete(post);
        postRepository.flush();

        assertThat(commentRepository.count()).isZero();
    }
}
