// ItemServiceImpl.java
package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.dto.Item;
import com.example.demo.repository.ItemRepository;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;

    public ItemServiceImpl(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Item> list() {
        return repository.findAll();
    }

    @Override
    public Item get(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Item create(Item item) {
        return repository.save(item);
    }

    @Override
    public Item update(Long id, Item item) {
        return repository.update(id, item);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}