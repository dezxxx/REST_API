package com.dezxxx.rest.validation;

import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityValidatorTest {

    // --- validateForCreate (User) ---

    @Test
    void validateForCreate_shouldPass_whenValid() {
        assertDoesNotThrow(() -> EntityValidator.validateForCreate(new User("Ivan")));
    }

    @Test
    void validateForCreate_shouldThrow_whenUserIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validateForCreate(null));
    }

    @Test
    void validateForCreate_shouldThrow_whenNameIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validateForCreate(new User()));
    }

    @Test
    void validateForCreate_shouldThrow_whenNameIsBlank() {
        assertThrows(ValidationException.class, () -> EntityValidator.validateForCreate(new User("   ")));
    }

    @Test
    void validateForCreate_shouldThrow_whenNameExceedsMaxLength() {
        assertThrows(ValidationException.class, () -> EntityValidator.validateForCreate(new User("A".repeat(256))));
    }

    // --- validateForUpdate (User) ---

    @Test
    void validateForUpdate_shouldPass_whenNameValid() {
        User user = new User("Ivan");
        assertDoesNotThrow(() -> EntityValidator.validateForUpdate(user));
    }

    @Test
    void validateForUpdate_shouldThrow_whenNameIsBlank() {
        User user = new User("   ");
        assertThrows(ValidationException.class, () -> EntityValidator.validateForUpdate(user));
    }

    // --- validate (File) ---

    @Test
    void validateFile_shouldPass_whenValid() {
        assertDoesNotThrow(() -> EntityValidator.validate(new File("report.pdf", "/uploads/report.pdf")));
    }

    @Test
    void validateFile_shouldThrow_whenFileIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validate((File) null));
    }

    @Test
    void validateFile_shouldThrow_whenNameIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validate(new File(null, "/uploads/report.pdf")));
    }

    @Test
    void validateFile_shouldThrow_whenNameIsBlank() {
        assertThrows(ValidationException.class, () -> EntityValidator.validate(new File("   ", "/uploads/report.pdf")));
    }

    @Test
    void validateFile_shouldThrow_whenNameExceedsMaxLength() {
        assertThrows(ValidationException.class,
                () -> EntityValidator.validate(new File("A".repeat(256), "/uploads/report.pdf")));
    }

    // --- validate (Event) ---

    @Test
    void validateEvent_shouldPass_whenValid() {
        User user = new User("Ivan");
        user.setId(1);
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(1);
        assertDoesNotThrow(() -> EntityValidator.validate(new Event(user, file)));
    }

    @Test
    void validateEvent_shouldThrow_whenEventIsNull() {
        assertThrows(ValidationException.class, () -> EntityValidator.validate((Event) null));
    }

    @Test
    void validateEvent_shouldThrow_whenUserIsNull() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(1);
        assertThrows(ValidationException.class, () -> EntityValidator.validate(new Event(null, file)));
    }

    @Test
    void validateEvent_shouldThrow_whenUserIdIsNull() {
        assertThrows(ValidationException.class,
                () -> EntityValidator.validate(new Event(new User("Ivan"), new File("report.pdf", "/path"))));
    }

    @Test
    void validateEvent_shouldThrow_whenFileIsNull() {
        User user = new User("Ivan");
        user.setId(1);
        assertThrows(ValidationException.class, () -> EntityValidator.validate(new Event(user, null)));
    }
}
