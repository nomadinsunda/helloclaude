package com.example.board.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.board.global.config.JpaAuditingConfig;
import com.example.board.post.domain.Post;
import com.example.board.support.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 저장하면_생성_수정_시각이_기록된다() {
        Post saved = postRepository.saveAndFlush(Post.create("제목", "내용", "작성자"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void 생성_수정_시각은_마이크로초로_잘려_DB_값과_일치한다() {
        Post saved = postRepository.saveAndFlush(Post.create("제목", "내용", "작성자"));
        saved.update("새 제목", "새 내용");
        postRepository.flush();
        LocalDateTime createdAt = saved.getCreatedAt();
        LocalDateTime updatedAt = saved.getUpdatedAt();
        entityManager.clear();

        Post reloaded = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(createdAt.getNano() % 1_000).isZero();
        assertThat(updatedAt.getNano() % 1_000).isZero();
        assertThat(reloaded.getCreatedAt()).isEqualTo(createdAt);
        assertThat(reloaded.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void 최신순으로_페이징_조회한다() {
        for (int i = 1; i <= 3; i++) {
            postRepository.saveAndFlush(Post.create("제목" + i, "내용", "작성자"));
        }

        Page<Post> page = postRepository.findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(Post::getTitle).containsExactly("제목3", "제목2");
    }
}
