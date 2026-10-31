package com.example.mediacollection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作者建立/更新請求 DTO。
 *
 * 職責：接收前端 JSON 請求體，用於 POST（新增）與 PUT（更新）操作。
 * 設計原則：只含業務欄位，不含 id、createdAt、updatedAt 等系統欄位。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRequestDTO {

    /** 作者名稱（必填） */
    private String name;

    /** 作者簡介（選填） */
    private String biography;
}
