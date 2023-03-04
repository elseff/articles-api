package com.elseff.project.persistense;


import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "article", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    Timestamp createdAt;

    @Column(name = "edited", nullable = false)
    Boolean edited;

    @Column(name = "updated_at")
    Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    User author;

    @PrePersist
    void init(){
        this.createdAt = Timestamp.from(Instant.now());
        if(this.getEdited() == null)
            this.setEdited(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleEntity article = (ArticleEntity) o;
        return id.equals(article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
