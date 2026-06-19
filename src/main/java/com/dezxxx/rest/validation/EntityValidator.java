package com.dezxxx.rest.validation;

import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;

public final class EntityValidator {

    private EntityValidator() {
    }

    public static void validate(User user) {
        if (user == null) {
            throw new ValidationException("User must not be null");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("User name must not be blank");
        }
    }

    public static void validate(File file) {
        if (file == null) {
            throw new ValidationException("File must not be null");
        }
        if (file.getName() == null || file.getName().isBlank()) {
            throw new ValidationException("File name must not be blank");
        }
        if (file.getFilePath() == null || file.getFilePath().isBlank()) {
            throw new ValidationException("File path must not be blank");
        }
    }

    public static void validate(Event event) {
        if (event == null) {
            throw new ValidationException("Event must not be null");
        }
        if (event.getUser() == null || event.getUser().getId() == null) {
            throw new ValidationException("Event must have a valid user id");
        }
        if (event.getFile() == null || event.getFile().getId() == null) {
            throw new ValidationException("Event must have a valid file id");
        }
    }
}
