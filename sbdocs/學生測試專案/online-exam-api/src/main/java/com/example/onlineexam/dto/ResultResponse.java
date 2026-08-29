package com.example.onlineexam.dto;

import java.time.LocalDateTime;

public record ResultResponse(
    Long id,
    Long examId,
    String examTitle,
    String studentName,
    Integer score,
    Integer totalPoints,
    double percentage,
    String grade,
    LocalDateTime submittedAt
) {}
