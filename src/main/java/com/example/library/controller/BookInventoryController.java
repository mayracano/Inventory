package com.example.library.controller;

import com.example.library.dto.BookInventoryDTO;
import com.example.library.dto.BookReservationDTO;
import com.example.library.dto.BookReservationEvent;
import com.example.library.dto.BookReservationStatus;
import com.example.library.service.BookInventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BookInventoryController {

    @Autowired
    private BookInventoryService inventoryService;

    @Autowired
    KafkaTemplate<String, BookReservationEvent> kafkaTemplate;

    @KafkaListener(topics = "add-inventory", groupId = "reservations-group")
    public void addToInventory(String event) throws Exception {

        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        BookReservationDTO bookReservationDTO = bookReservationEvent.getBookReservation();
        BookInventoryDTO bookInventoryDTO = new BookInventoryDTO();
        bookInventoryDTO.setReservationID(bookReservationDTO.getId());
        bookInventoryDTO.setBookId(bookReservationDTO.getBookId());
        bookInventoryDTO.setUserId(bookReservationDTO.getUserId());

        try {
            inventoryService.addToInventory(bookInventoryDTO);
            BookReservationEvent bookReservationCompleteEvent = new BookReservationEvent();
            bookReservationCompleteEvent.setBookReservation(bookReservationDTO);
            bookReservationCompleteEvent.setBookReservationStatus(BookReservationStatus.COMPLETED);
            kafkaTemplate.send("completed-inventory", bookReservationCompleteEvent);
        } catch(Exception e) {
            BookReservationEvent bookReservationReverseEvent = new BookReservationEvent();
            bookReservationReverseEvent.setBookReservation(bookReservationDTO);
            bookReservationReverseEvent.setBookReservationStatus(BookReservationStatus.REVERSED);
            kafkaTemplate.send("reversed-inventory", bookReservationReverseEvent);
        }
    }
}
