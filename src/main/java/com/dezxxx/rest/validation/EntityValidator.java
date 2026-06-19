package com.dezxxx.rest.validation;

import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;

public final class EntityValidator {

    private EntityValidator() {
    }

    public static void validate(User user) {
        requireNonNull(user, "User must not be null");
        requireNonBlank(user.getName(), "User name must not be blank");
    }

    public static void validate(File file) {
        requireNonNull(file, "File must not be null");
        requireNonBlank(file.getName(), "File name must not be blank");
        requireNonBlank(file.getFilePath(), "File path must not be blank");
    }

    public static void validate(Event event) {
        requireNonNull(event, "Event must not be null");
        requireNonNull(event.getUser(), "Event must have a valid user id");
        requireNonNull(event.getUser().getId(), "Event must have a valid user id");
        requireNonNull(event.getFile(), "Event must have a valid file id");
        requireNonNull(event.getFile().getId(), "Event must have a valid file id");
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) throw new ValidationException(message);
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) throw new ValidationException(message);
    }
}
