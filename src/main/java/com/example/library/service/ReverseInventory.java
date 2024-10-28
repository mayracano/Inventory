package com.example.library.service;

import java.util.Optional;

import com.example.library.dto.BookReservationEvent;
import com.example.library.model.BookInventory;
import com.example.library.repository.BookInventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class ReverseInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseInventory.class);
    @Autowired
    private BookInventoryRepository bookInventoryRepository;

    @KafkaListener(topics = "reversed-inventory", groupId = "inventory-group")
    public void reverseInventory(String event) {

        try {
            BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
            LOGGER.info(String.format("Received 'reversed-inventory', operation to reverse the remove of a book from the inventory for user: %s and book: %s", bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId()));

            Optional<BookInventory> optionalInventory = bookInventoryRepository.findByBookId(bookReservationEvent.getBookReservation().getBookId());
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
