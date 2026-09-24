package com.example.cms.media.repository;

import com.example.cms.media.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);

    Page<Media> findAllByOrderByCreatedAtDesc(Pageable pageable);
}