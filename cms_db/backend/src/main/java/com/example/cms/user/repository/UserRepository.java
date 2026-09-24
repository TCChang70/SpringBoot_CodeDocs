package com.example.cms.user.repository;

import com.example.cms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("select case when count(a) > 0 then true else false end from Article a where a.author.id = :userId")
    boolean existsByAuthorId(@Param("userId") Long userId);
}