// ItemService.java
package com.example.demo.service;

import java.util.List;
import com.example.demo.dto.Item;

public interface ItemService {
    List<Item> list();
    Item get(Long id);
    Item create(Item item);
    Item update(Long id, Item item);
    void delete(Long id);
}
