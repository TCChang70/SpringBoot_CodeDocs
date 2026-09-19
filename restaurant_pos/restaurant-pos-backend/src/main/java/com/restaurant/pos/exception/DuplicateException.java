package com.restaurant.pos.exception;

public class DuplicateException extends BusinessException {

    public DuplicateException(String message) {
        super(4091, message);
    }
}