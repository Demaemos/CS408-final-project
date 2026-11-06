package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.NewsPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NewsPostRepository extends JpaRepository<NewsPost, Long> {
    Page<NewsPost> findAllByOrderByPublishedAtDesc(Pageable pageable);

    @Query("""
        select n from NewsPost n
        where (:keyword is null
            or lower(n.title) like concat('%', :keyword, '%')
            or lower(n.body) like concat('%', :keyword, '%')
            or lower(n.tags) like concat('%', :keyword, '%'))
          and (:tag is null
            or lower(concat(',', n.tags, ',')) like concat('%,', :tag, ',%'))
        order by n.publishedAt desc
        """)
    Page<NewsPost> search(@Param("keyword") String keyword,
                          @Param("tag") String tag,
                          Pageable pageable);

    @Query("select n.tags from NewsPost n where n.tags is not null and n.tags <> ''")
    java.util.List<String> findAllTagValues();

    Optional<NewsPost> findBySlug(String slug);
}
