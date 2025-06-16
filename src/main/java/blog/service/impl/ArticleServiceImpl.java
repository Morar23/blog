package blog.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import blog.model.ArticleModel;
import blog.entity.Article;
import blog.entity.Category;
import blog.entity.Tag;
import blog.entity.User;
import blog.repository.ArticleRepository;
import blog.repository.CategoryRepository;
import blog.repository.TagRepository;
import blog.repository.UserRepository;
import blog.service.ArticleService;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static blog.util.StringUtils.*;

@Service
@AllArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final TagRepository tagRepository;

    @Override
    public String loadCreateArticleView(Model model){
        List<Category> categories = this.categoryRepository.findAll();

        model.addAttribute(CATEGORIES, categories);
        model.addAttribute(VIEW, ARTICLE_CREATE);

        return BASE_LAYOUT;
    }

    @Override
    public String createArticle(ArticleModel articleModel) throws IOException {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User userEntity = this.userRepository
            .findByEmail(principal.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(
                MessageFormat.format(INVALID_USERNAME, principal.getUsername())
            ));

        Category category = this.categoryRepository
                .findById(articleModel.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                    MessageFormat.format(INVALID_CATEGORY_ID, articleModel.getCategoryId())
                ));

        List<Tag> tags = this.findTagsFromString(articleModel.getTagString());

        Article articleEntity = Article
                .builder()
                .title(articleModel.getTitle())
                .content(articleModel.getContent())
                .author(userEntity)
                .category(category)
                .tags(tags)
                .build();

        if(!articleModel.getPicture().isEmpty()){
            byte[] pictureBytes = articleModel.getPicture().getBytes();
            String pictureBase64 = Base64.getEncoder().encodeToString(pictureBytes);
            articleEntity.setPicture(pictureBase64);
        }

        this.articleRepository.saveAndFlush(articleEntity);

        return REDIRECT_HOME;
    }

    @Override
    public String loadArticleDetailsView(Model model, Integer id){
        if(!this.articleRepository.existsById(id)){
            return REDIRECT_HOME;
        }

        if(!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)){
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            User entityUser = this.userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                    MessageFormat.format(INVALID_USERNAME, principal.getUsername())
                ));

            model.addAttribute(USER, entityUser);
        }

        Article article = this.articleRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_ARTICLE_ID, id))
        );

        model.addAttribute(ARTICLE, article);
        model.addAttribute(VIEW, ARTICLE_DETAILS);

        return BASE_LAYOUT;
    }

    @Override
    public String loadArticleEditView(Integer id, Model model){
        if(!this.articleRepository.existsById(id)){
            return REDIRECT_HOME;
        }

        Article article = this.articleRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException(MessageFormat.format(INVALID_ARTICLE_ID, id))
        );

        if (neitherAuthorOrAdmin(article)){
            return MessageFormat.format(REDIRECT_ARTICLES_ID, id);
        }

        List<Category> categories = this.categoryRepository.findAll();

        String tagString = article.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));

        model.addAttribute(VIEW, ARTICLE_EDIT);
        model.addAttribute(ARTICLE, article);
        model.addAttribute(CATEGORIES, categories);
        model.addAttribute(TAGS, tagString);

        return BASE_LAYOUT;
    }

    @Override
    public String editArticle(Integer id, ArticleModel articleModel) throws IOException {
        if(!this.articleRepository.existsById(id)){
            return REDIRECT_HOME;
        }

        Article article = this.articleRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_ARTICLE_ID, id))
        );

        if (neitherAuthorOrAdmin(article)){
            return MessageFormat.format(REDIRECT_ARTICLES_ID, id);
        }

        Category category = this.categoryRepository
                .findById(articleModel.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                    MessageFormat.format(INVALID_CATEGORY_ID, articleModel.getCategoryId())
                ));

        List<Tag> tags = this.findTagsFromString(articleModel.getTagString());

        if(!articleModel.getPicture().isEmpty()){
            byte[] pictureBytes = articleModel.getPicture().getBytes();
            String pictureBase64 = Base64.getEncoder().encodeToString(pictureBytes);
            article.setPicture(pictureBase64);
        }

        article.setTags(tags);
        article.setCategory(category);
        article.setContent(articleModel.getContent());
        article.setTitle(articleModel.getTitle());

        this.articleRepository.saveAndFlush(article);

        return MessageFormat.format(REDIRECT_ARTICLES_ID, article.getId());
    }

    @Override
    public String loadArticleDeleteView(Model model, Integer id){
        if(!this.articleRepository.existsById(id)){
            return REDIRECT_HOME;
        }

        Article article = this.articleRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_ARTICLE_ID, id))
        );

        if (neitherAuthorOrAdmin(article)){
            return MessageFormat.format(REDIRECT_ARTICLES_ID, id);
        }

        model.addAttribute(ARTICLE, article);
        model.addAttribute(VIEW, ARTICLE_DELETE);

        return BASE_LAYOUT;
    }

    @Override
    public String deleteArticle(Integer id){
        if(!this.articleRepository.existsById(id)){
            return REDIRECT_HOME;
        }

        Article article = this.articleRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_ARTICLE_ID, id))
        );

        if (neitherAuthorOrAdmin(article)){
            return MessageFormat.format(REDIRECT_ARTICLES_ID, id);
        }

        this.articleRepository.delete(article);

        return REDIRECT_HOME;
    }

    private List<Tag> findTagsFromString(String tagString){
        List<Tag> tags = new LinkedList<>();

        String[] tagNames = tagString.split(",\\s*");

        for (String tagName : tagNames){
            Tag currentTag = this.tagRepository.findByName(tagName);

            if(currentTag == null){
                currentTag = Tag.builder().name(tagName).build();
                this.tagRepository.saveAndFlush(currentTag);
            }

            tags.add(currentTag);
        }
        return tags;
    }

    private boolean neitherAuthorOrAdmin(Article article){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User userEntity = this.userRepository
            .findByEmail(principal.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(
                MessageFormat.format(INVALID_USERNAME, principal.getUsername())
            ));

        return !(userEntity.isAdmin() || userEntity.isAuthor(article));
    }
}
