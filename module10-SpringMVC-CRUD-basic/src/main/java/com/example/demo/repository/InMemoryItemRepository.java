// InMemoryItemRepository.java
package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.example.demo.dto.Item;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private long sequence = 0L;                
    private final List<Item> store = new ArrayList<>(); 

    public InMemoryItemRepository() {
        // 샘플 데이터
        save(new Item(null, "노트북", 1500, 3));
        save(new Item(null, "핸드폰", 500, 10));
    }

    @Override
    public List<Item> findAll() {
        return store; 
    }

    @Override
    public Optional<Item> findById(Long id) {
        return store.stream().filter(i -> i.getId().equals(id)).findFirst();
    }

    @Override
    public Item save(Item item) {
        item.setId(++sequence); // 단순 증가
        store.add(item);
        return item;
    }

    @Override
    public Item update(Long id, Item updateParam) {
        Item target = findById(id).orElseThrow();
        target.setName(updateParam.getName());
        target.setPrice(updateParam.getPrice());
        target.setQuantity(updateParam.getQuantity());
        return target;
    }

    @Override
    public void deleteById(Long id) {
        store.removeIf(i -> i.getId().equals(id));
    }
}