package com.dezxxx.rest.exception;

public class EntityNotFoundException extends AppException {

    public EntityNotFoundException(String entityName, Integer id) {
        super(entityName + " not found with id: " + id);
    }
}