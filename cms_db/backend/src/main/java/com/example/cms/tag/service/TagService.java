package com.example.cms.tag.service;

import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.tag.dto.TagRequest;
import com.example.cms.tag.dto.TagResponse;
import com.example.cms.tag.entity.Tag;
import com.example.cms.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 標籤管理（FR-04）。
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagResponse> list() {
        return tagRepository.findAll().stream().map(TagResponse::from).toList();
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }
        if (tagRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.SLUG_DUPLICATED);
        }
        Tag tag = new Tag();
        tag.setName(request.name());
        tag.setSlug(request.slug());
        return TagResponse.from(tagRepository.save(tag));
    }

    @Transactional
    public TagResponse update(Integer id, TagRequest request) {
        Tag tag = getTag(id);
        if (!tag.getName().equals(request.name()) && tagRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }
        if (!tag.getSlug().equals(request.slug()) && tagRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.SLUG_DUPLICATED);
        }
        tag.setName(request.name());
        tag.setSlug(request.slug());
        return TagResponse.from(tagRepository.save(tag));
    }

    @Transactional
    public void delete(Integer id) {
        // 中介表關聯由 DB CASCADE 移除
        tagRepository.delete(getTag(id));
    }

    private Tag getTag(Integer id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
    }
}