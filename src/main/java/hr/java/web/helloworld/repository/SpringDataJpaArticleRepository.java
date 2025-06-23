package hr.java.web.helloworld.repository;

import hr.java.web.helloworld.domain.Article;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Primary
@Repository
public interface SpringDataJpaArticleRepository
        extends JpaRepository<Article, Integer>, JpaSpecificationExecutor<Article> {
    List<Article> findByName(String name);
}
