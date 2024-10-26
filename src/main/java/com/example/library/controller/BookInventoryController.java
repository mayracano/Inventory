package com.example.library.controller;

import com.example.library.dto.BookInventoryDTO;
import com.example.library.dto.BookReservation;
import com.example.library.dto.BookReservationEvent;
import com.example.library.dto.BookReservationStatus;
import com.example.library.service.BookInventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BookInventoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookInventoryController.class);

    @Autowired
    private BookInventoryService inventoryService;

    @Autowired
    KafkaTemplate<String, BookReservationEvent> kafkaTemplate;

    @KafkaListener(topics = "removed-inventory", groupId = "reservations-group")
    public void addToInventory(String event) throws Exception {

        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        LOGGER.info(String.format("Received 'removed-inventory', operation to remove a book from the inventory for user: %s and book: %s", bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId()));

        BookReservation bookReservation = bookReservationEvent.getBookReservation();
        BookInventoryDTO bookInventoryDTO = new BookInventoryDTO();
        bookInventoryDTO.setReservationID(bookReservation.getId());
        bookInventoryDTO.setBookId(bookReservation.getBookId());
        bookInventoryDTO.setUserId(bookReservation.getUserId());

        try {
            inventoryService.removeFromInventory(bookInventoryDTO);
            BookReservationEvent bookReservationCompleteEvent = new BookReservationEvent();
            bookReservationCompleteEvent.setBookReservation(bookReservation);
            bookReservationCompleteEvent.setBookReservationStatus(BookReservationStatus.COMPLETED);
            kafkaTemplate.send("completed-inventory", bookReservationEvent);
            LOGGER.info(String.format("Sent 'completed-inventory' for user: %s and book: %s", bookReservationCompleteEvent.getBookReservation().getBookId(), bookReservationCompleteEvent.getBookReservation().getUserId()));
        } catch(Exception e) {
            BookReservationEvent bookReservationReverseEvent = new BookReservationEvent();
            bookReservationReverseEvent.setBookReservation(bookReservation);
            bookReservationReverseEvent.setBookReservationStatus(BookReservationStatus.REVERSED);
            kafkaTemplate.send("removed-inventory-failed", bookReservationEvent);
            LOGGER.info(String.format("Sent 'removed-inventory-failed' for user: %s and book: %s", bookReservationReverseEvent.getBookReservation().getBookId(), bookReservationReverseEvent.getBookReservation().getUserId()));
        }
    }
}
