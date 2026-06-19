package com.dezxxx.rest.service;

import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.repository.Repository;

import java.util.List;

public interface EventService extends Repository<Event> {

    List<Event> findByUserId(Integer userId);
}
