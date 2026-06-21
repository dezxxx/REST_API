ALTER TABLE events
    ADD CONSTRAINT uq_event_user_file UNIQUE (user_id, file_id);