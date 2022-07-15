package com.elseff.project.controller.article;

import com.elseff.project.dto.article.ArticleDto;
import com.elseff.project.exception.IdLessThanZeroException;
import com.elseff.project.service.article.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/articles")
@CrossOrigin(origins = {"http://192.168.100.3:4200", "http://localhost:4200"})
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }
    @GetMapping()
    public List<ArticleDto> getArticles(){
        return articleService.getAllArticles();
    }

    @PostMapping()
    public ArticleDto addArticle(@RequestBody @Valid ArticleDto articleDto) {
        System.out.println(articleDto.toString());
        return articleService.addArticle(articleDto);
    }

    @GetMapping("/{id}")
    public ArticleDto getSpecific(@PathVariable Long id) {
        if (id < 0) {
            throw new IdLessThanZeroException();
        }
        return articleService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable Long id) {
        articleService.deleteArticleById(id);
    }

    @PatchMapping("/{id}")
    public ArticleDto updateArticle(@RequestBody @Valid ArticleDto articleDto, @PathVariable Long id){
        return articleService.updateArticle(id, articleDto);
    }
}
