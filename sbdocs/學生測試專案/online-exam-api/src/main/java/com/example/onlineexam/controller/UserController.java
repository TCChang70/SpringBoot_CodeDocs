package com.example.onlineexam.controller;

import com.example.onlineexam.dto.StudentResponse;
import com.example.onlineexam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** 列出所有學生，可選用 className 篩選 */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentResponse>> getStudents(
            @RequestParam(required = false) String className) {
        var users = (className != null && !className.isBlank())
                ? userRepository.findByRoleAndClassNameOrderByDisplayNameAsc("ROLE_STUDENT", className)
                : userRepository.findByRoleOrderByClassNameAscDisplayNameAsc("ROLE_STUDENT");
        return ResponseEntity.ok(users.stream()
                .map(u -> new StudentResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getClassName()))
                .toList());
    }

    /** 取得所有班級名稱清單 */
    @GetMapping("/classes")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<String>> getClasses() {
        return ResponseEntity.ok(userRepository.findDistinctStudentClasses());
    }
}
