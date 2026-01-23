// ItemController.java
package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Item;
import com.example.demo.service.ItemService;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

     // 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", service.list());
        return "items/list";
    }

    // 등록 폼
    @GetMapping("/new")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "items/add";
    }

    // 등록 처리
    @PostMapping
    public String create(@ModelAttribute("item") Item item) {
        service.create(item);
        return "redirect:/items";
    }

    // 상세 보기
    @GetMapping("/view")
    public String view(@RequestParam("id") Long id, Model model) {
        model.addAttribute("item", service.get(id));
        return "items/view";
    }

    // 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("item", service.get(id));
        return "items/edit";
    }

    // 수정 처리
    @PostMapping("/edit")
    public String update(@ModelAttribute("item") Item item) {
        service.update(item.getId(), item);
        return "redirect:/items/view?id=" + item.getId();
    }

    // 삭제 처리
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        service.delete(id);
        return "redirect:/items";
    }

}
