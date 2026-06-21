package com.dezxxx.rest.validation;

import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityValidatorTest {

    @Test
    void validateUser_shouldPass_whenValid() {
        assertDoesNotThrow(() -> EntityValidator.validate(new User("Ivan")));
    }

    @Test
    void validateUser_shouldThrow_whenNameIsNull() {
        User user = new User();
        assertThrows(ValidationException.class, () -> EntityValidator.validate(user));
    }

    @Test
    void validateUser_shouldThrow_whenNameIsBlank() {
        User user = new User("   ");
        assertThrows(ValidationException.class, () -> EntityValidator.validate(user));
    }

    @Test
    void validateUser_shouldThrow_whenUserIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validate((User) null));
    }

    @Test
    void validateFile_shouldPass_whenValid() {
        assertDoesNotThrow(() -> EntityValidator.validate(new File("report.pdf", "/uploads/report.pdf")));
    }

    @Test
    void validateFile_shouldThrow_whenNameIsNull() {
        File file = new File(null, "/uploads/report.pdf");
        assertThrows(ValidationException.class, () -> EntityValidator.validate(file));
    }

    @Test
    void validateFile_shouldThrow_whenFilePathIsBlank() {
        File file = new File("report.pdf", "   ");
        assertThrows(ValidationException.class, () -> EntityValidator.validate(file));
    }

    @Test
    void validateFile_shouldThrow_whenFileIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validate((File) null));
    }

    @Test
    void validateEvent_shouldPass_whenValid() {
        User user = new User("Ivan");
        user.setId(1);
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(1);
        assertDoesNotThrow(() -> EntityValidator.validate(new Event(user, file)));
    }

    @Test
    void validateEvent_shouldThrow_whenUserIsNull() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(1);
        Event event = new Event(null, file);
        assertThrows(ValidationException.class, () -> EntityValidator.validate(event));
    }

    @Test
    void validateEvent_shouldThrow_whenUserIdIsNull() {
        Event event = new Event(new User("Ivan"), new File("report.pdf", "/uploads/report.pdf"));
        assertThrows(ValidationException.class, () -> EntityValidator.validate(event));
    }

    @Test
    void validateEvent_shouldThrow_whenFileIsNull() {
        User user = new User("Ivan");
        user.setId(1);
        Event event = new Event(user, null);
        assertThrows(ValidationException.class, () -> EntityValidator.validate(event));
    }

    @Test
    void validateEvent_shouldThrow_whenEventIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validate((Event) null));
    }

    @Test
    void validateUser_shouldThrow_whenNameExceedsMaxLength() {
        User user = new User("A".repeat(256));
        assertThrows(ValidationException.class, () -> EntityValidator.validate(user));
    }

    @Test
    void validateFile_shouldThrow_whenNameExceedsMaxLength() {
        File file = new File("A".repeat(256), "/uploads/report.pdf");
        assertThrows(ValidationException.class, () -> EntityValidator.validate(file));
    }

    @Test
    void validateFile_shouldThrow_whenFilePathExceedsMaxLength() {
        File file = new File("report.pdf", "/".repeat(501));
        assertThrows(ValidationException.class, () -> EntityValidator.validate(file));
    }
}
