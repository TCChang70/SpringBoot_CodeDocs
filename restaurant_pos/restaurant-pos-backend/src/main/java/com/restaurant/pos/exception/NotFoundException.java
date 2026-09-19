package com.restaurant.pos.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(4041, message);
    }
}