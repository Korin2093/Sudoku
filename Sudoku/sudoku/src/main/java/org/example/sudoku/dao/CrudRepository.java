package org.example.sudoku.dao;

import java.util.*;

public interface CrudRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}