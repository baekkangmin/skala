// ItemRepository.java
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.dto.Item;

public interface ItemRepository {
    List<Item> findAll();
    Optional<Item> findById(Long id);
    Item save(Item item);
    Item update(Long id, Item updateParam);
    void deleteById(Long id);
}
