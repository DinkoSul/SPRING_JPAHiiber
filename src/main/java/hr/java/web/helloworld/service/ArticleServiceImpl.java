package hr.java.web.helloworld.service;

import hr.java.web.helloworld.domain.Article;
import hr.java.web.helloworld.domain.Category;
import hr.java.web.helloworld.domain.SearchArticle;
import hr.java.web.helloworld.dto.ArticleDTO;
import hr.java.web.helloworld.dto.SearchArticleDTO;
import hr.java.web.helloworld.repository.SpringDataCategoryRepository;
import hr.java.web.helloworld.repository.SpringDataJpaArticleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    //private ArticleRepository articleRepository;
    private SpringDataJpaArticleRepository articleRepository;
    private SpringDataCategoryRepository categoryRepository;

    @Override
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::convertArticleToArticleDTO)
                .toList();
        /*
        return articleRepository.getAllArticles().stream()
                .map(this::convertArticleToArticleDTO)
                .toList();

         */
    }

    @Override
    public List<ArticleDTO> getArticlesByName(String articleName) {
        return articleRepository.findByName(articleName).stream()
                .map(this::convertArticleToArticleDTO)
                .toList();
        /*
        return articleRepository.getArticlesByName(articleName).stream()
                .map(this::convertArticleToArticleDTO)
                .toList();

         */
    }

    @Override
    public ArticleDTO saveNewArticle(ArticleDTO article) {
        return convertArticleToArticleDTO(articleRepository.save(convertArticleDtoToArticle(article)));
        //return convertArticleToArticleDTO(articleRepository.saveNewArticle(convertArticleDtoToArticle(article)));
    }

    @Override
    public List<ArticleDTO> filterByParameters(SearchArticleDTO searchArticleDTO) {
        Article article = new Article();
        article.setName(searchArticleDTO.getArticleName());
        article.setDescription(searchArticleDTO.getArticleDescription());
        article.setPrice(searchArticleDTO.getLowerPrice() != null ? searchArticleDTO.getLowerPrice() : null);
        article.setCategory(categoryRepository.findByName(searchArticleDTO.getCategoryName()));

        Specification<Article> spec = Specification.where(null);

        if (searchArticleDTO.getArticleName() != null && !searchArticleDTO.getArticleName().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), searchArticleDTO.getArticleName()));
        }

        if (searchArticleDTO.getArticleDescription() != null && !searchArticleDTO.getArticleDescription().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("description"), searchArticleDTO.getArticleDescription()));
        }

        if (searchArticleDTO.getLowerPrice() != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), searchArticleDTO.getLowerPrice()));
        }

        if (searchArticleDTO.getUpperPrice() != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), searchArticleDTO.getUpperPrice()));
        }

        if (searchArticleDTO.getCategoryName() != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), searchArticleDTO.getCategoryName()));
        }

        return articleRepository.findAll(spec).stream()
                .map(this::convertArticleToArticleDTO)
                .toList();

        /*
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("description", ExampleMatcher.GenericPropertyMatchers.exact());

        if (searchArticleDTO.getLowerPrice() != null) {
            matcher = matcher.withMatcher("price",
                    ExampleMatcher.GenericPropertyMatchers.greaterThanOrEqualTo(searchArticleDTO.getLowerPrice()));
        }
        if (searchArticleDTO.getUpperPrice() != null) {
            matcher = matcher.withMatcher("price",
                    ExampleMatcher.GenericPropertyMatchers.lessThanOrEqualTo(searchArticleDTO.getLowerPrice()));
        }

        Example<Article> example = Example.of(article, matcher);
        return articleRepository.findAll(example).stream()
                .map(this::convertArticleToArticleDTO)
                .toList();

         */


        /*
        return articleRepository.filterByParameters(
                convertSearchArticleDtoToSearchArticle(searchArticleDTO))
                .stream().map(this::convertArticleToArticleDTO)
                .toList();

         */
    }

    @Override
    public Optional<ArticleDTO> updateArticle(ArticleDTO articleDTO, Integer id) {

        Optional<Article> articleToUpdate = articleRepository.findById(id);

        if (articleToUpdate.isPresent()) {
            Article article = articleToUpdate.get();
            article.setCategory(categoryRepository.findByName(articleDTO.getCategoryName()));
            article.setPrice(articleDTO.getArticlePrice());
            article.setDescription(articleDTO.getArticleDescription());
            article.setName(articleDTO.getArticleName());
            Article updatedArticle = articleRepository.save(article);
            return Optional.of(convertArticleToArticleDTO(updatedArticle));
        }

        return Optional.empty();
    }

    @Override
    public boolean articleByIdExists(Integer id) {
        return articleRepository.findById(id).isPresent();
    }

    @Override
    public boolean deleteArticleById(Integer id) {
        if (articleByIdExists(id)) {
            articleRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    private ArticleDTO convertArticleToArticleDTO(Article article) {
        return new ArticleDTO(article.getName(),
                article.getDescription(), article.getPrice(),
                article.getCategory().getName());
    }

    private Article convertArticleDtoToArticle(ArticleDTO articleDTO) {
        Integer latestId =
                articleRepository.findAll().stream()
                        .max((a1, a2) -> a1.getId().compareTo(a2.getId()))
                        .get().getId();

        return new Article(latestId + 1, articleDTO.getArticleName(),
                articleDTO.getArticleDescription(), articleDTO.getArticlePrice(),
                categoryRepository.findByName(articleDTO.getCategoryName()));
    }

    private SearchArticle convertSearchArticleDtoToSearchArticle(SearchArticleDTO searchArticleDTO) {
        return new SearchArticle(
                searchArticleDTO.getArticleName(),
                searchArticleDTO.getArticleDescription(),
                searchArticleDTO.getLowerPrice(),
                searchArticleDTO.getUpperPrice(),
                categoryRepository.findByName(searchArticleDTO.getCategoryName()));
    }
}
