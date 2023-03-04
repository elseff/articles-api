package com.elseff.project.persistense.dao;

import com.elseff.project.persistense.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {

    List<ArticleEntity> findAllByAuthorId(Long authorId);
}
