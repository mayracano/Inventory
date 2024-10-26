package com.example.library.service;

import java.util.Optional;

import com.example.library.dto.BookInventoryEvent;
import com.example.library.model.BookInventory;
import com.example.library.repository.BookInventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
public class ReverseInventory {

    @Autowired
    private BookInventoryRepository bookInventoryRepository;

    @KafkaListener(topics = "reverse-inventory", groupId = "inventory-group")
    public void reverseInventory(String event) {
        try {
            BookInventoryEvent bookInventoryEvent = new ObjectMapper().readValue(event, BookInventoryEvent.class);
            Optional<BookInventory> optionalInventory = bookInventoryRepository.findByBookId(bookInventoryEvent.getBookInventory().getBookId());
            optionalInventory.ifPresent(bookInventory -> {
                if (bookInventory.getQuantity() > 0) {
                    int newQuantity = bookInventory.getQuantity() - 1;
                    bookInventory.setQuantity(newQuantity);
                    bookInventoryRepository.save(bookInventory);
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
            //TODO: Exceptions handling
        }
    }
}
