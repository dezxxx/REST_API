package com.dezxxx.rest.repository;

import com.dezxxx.rest.model.Event;

import java.util.List;

public interface EventRepository extends Repository<Event> {

    List<Event> findByUserId(Integer userId);

    boolean existsByUserAndFile(Integer userId, Integer fileId);

    boolean existsByFileId(Integer fileId);
}