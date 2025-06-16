package blog.controller;

import blog.service.ArticleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import blog.model.ArticleModel;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/article")
@AllArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/{id}")
    public String details(Model model, @PathVariable Integer id){
        return this.articleService.loadArticleDetailsView(model, id);
    }

    @GetMapping("/create")
    public String create(Model model){
        return this.articleService.loadCreateArticleView(model);
    }

    @PostMapping("/create")
    public String createProcess(ArticleModel articleModel) throws IOException {
        return this.articleService.createArticle(articleModel);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        return this.articleService.loadArticleEditView(id, model);
    }

    @PostMapping("/edit/{id}")
    public String editProcess(@PathVariable Integer id, ArticleModel articleModel) throws IOException {
        return this.articleService.editArticle(id, articleModel);
    }

    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Integer id){
        return this.articleService.loadArticleDeleteView(model, id);
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id){
        return this.articleService.deleteArticle(id);
    }
}
