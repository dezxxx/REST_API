package com.dezxxx.rest.validation;

import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;

public final class EntityValidator {

    private EntityValidator() {
    }

    public static void validate(User user) {
        chain(user, "User")
                .notBlank(user.getName(), "User name")
                .maxLength(user.getName(), 255, "User name");
    }

    public static void validate(File file) {
        chain(file, "File")
                .notBlank(file.getName(), "File name")
                .maxLength(file.getName(), 255, "File name")
                .notBlank(file.getFilePath(), "File path")
                .maxLength(file.getFilePath(), 500, "File path");
    }

    public static void validate(Event event) {
        chain(event, "Event")
                .notNull(event.getUser(), "Event must have a valid user id")
                .notNull(event.getUser().getId(), "Event must have a valid user id")
                .notNull(event.getFile(), "Event must have a valid file id")
                .notNull(event.getFile().getId(), "Event must have a valid file id");
    }

    private static ValidationChain chain(Object root, String entityName) {
        if (root == null) throw new ValidationException(entityName + " must not be null");
        return new ValidationChain();
    }

    private static final class ValidationChain {

        ValidationChain notNull(Object value, String message) {
            if (value == null) throw new ValidationException(message);
            return this;
        }

        ValidationChain notBlank(String value, String fieldName) {
            if (value == null || value.isBlank())
                throw new ValidationException(fieldName + " must not be blank");
            return this;
        }

        ValidationChain maxLength(String value, int max, String fieldName) {
            if (value != null && value.length() > max)
                throw new ValidationException(fieldName + " must not exceed " + max + " characters");
            return this;
        }
    }
}
