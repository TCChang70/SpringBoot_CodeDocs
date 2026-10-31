package com.example.mediacollection.service.impl;

import com.example.mediacollection.dto.AuthorDTO;
import com.example.mediacollection.dto.AuthorRequestDTO;
import com.example.mediacollection.entity.Author;
import com.example.mediacollection.repository.AuthorRepository;
import com.example.mediacollection.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * AuthorService 介面的具體實作。
 *
 * 職責：管理作者資料的業務邏輯，包括 CRUD 操作與 DTO 轉換。
 */
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorDTO getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("找不到 ID 為 " + id + " 的作者"));
        return toDTO(author);
    }

    @Override
    @Transactional
    public AuthorDTO createAuthor(AuthorRequestDTO requestDTO) {
        Author author = toEntity(requestDTO);
        Author savedAuthor = authorRepository.save(author);
        return toDTO(savedAuthor);
    }

    @Override
    @Transactional
    public AuthorDTO updateAuthor(Long id, AuthorRequestDTO requestDTO) {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("找不到 ID 為 " + id + " 的作者"));

        existingAuthor.setName(requestDTO.getName());
        existingAuthor.setBiography(requestDTO.getBiography());

        Author updatedAuthor = authorRepository.save(existingAuthor);
        return toDTO(updatedAuthor);
    }

    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new NoSuchElementException("找不到 ID 為 " + id + " 的作者");
        }
        authorRepository.deleteById(id);
    }

    private AuthorDTO toDTO(Author author) {
        return AuthorDTO.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .createdAt(author.getCreatedAt())
                .updatedAt(author.getUpdatedAt())
                .build();
    }

    private Author toEntity(AuthorRequestDTO dto) {
        return Author.builder()
                .name(dto.getName())
                .biography(dto.getBiography())
                .build();
    }
}
