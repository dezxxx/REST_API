package com.dezxxx.rest.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {

    T create(T entity);

    Optional<T> findById(Integer id);

    List<T> findAll();

    T update(T entity);

    void delete(Integer id);
}