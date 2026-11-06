package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.model.NewsPost;
import com.tylerandrews.lcg.repository.NewsPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class NewsPostService {

    private final NewsPostRepository newsPostRepository;

    public NewsPostService(NewsPostRepository newsPostRepository) {
        this.newsPostRepository = newsPostRepository;
    }

    public Page<NewsPost> search(String keyword, String tag, int page, int size) {
        int pageSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize);
        String normalizedKeyword = normalize(keyword);
        String normalizedTag = normalize(tag);
        return newsPostRepository.search(
            normalizedKeyword.isEmpty() ? null : normalizedKeyword,
            normalizedTag.isEmpty() ? null : normalizedTag,
            pageable);
    }

    public Optional<NewsPost> findBySlug(String slug) {
        return newsPostRepository.findBySlug(slug);
    }

    public Optional<NewsPost> findById(Long id) {
        return newsPostRepository.findById(id);
    }

    public List<String> tags() {
        Set<String> tags = new LinkedHashSet<>();
        for (String postTags : newsPostRepository.findAllTagValues()) {
            for (String tag : postTags.split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tags.add(trimmed);
                }
            }
        }
        return new ArrayList<>(tags);
    }

    public List<NewsPost> latest(int limit) {
        int pageSize = Math.max(limit, 0);
        if (pageSize == 0) {
            return List.of();
        }
        return newsPostRepository.findAllByOrderByPublishedAtDesc(PageRequest.of(0, pageSize)).getContent();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
