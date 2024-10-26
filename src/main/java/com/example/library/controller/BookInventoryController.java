package com.example.library.controller;

import com.example.library.dto.BookInventoryDTO;
import com.example.library.dto.BookReservation;
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

    @KafkaListener(topics = "new-reservation", groupId = "reservations-group")
    public void addToInventory(String event) throws Exception {

        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        BookReservation bookReservation = bookReservationEvent.getBookReservation();
        BookInventoryDTO bookInventoryDTO = new BookInventoryDTO();
        bookInventoryDTO.setReservationID(bookReservation.getId());
        bookInventoryDTO.setBookId(bookReservation.getBookId());
        bookInventoryDTO.setUserId(bookReservation.getUserId());

        try {
            inventoryService.addToInventory(bookInventoryDTO);
            BookReservationEvent bookReservationCompleteEvent = new BookReservationEvent();
            bookReservationCompleteEvent.setBookReservation(bookReservation);
            bookReservationCompleteEvent.setBookReservationStatus(BookReservationStatus.COMPLETED);
            kafkaTemplate.send("completed-reservations", bookReservationEvent);
        } catch(Exception e) {
            BookReservationEvent bookReservationReverseEvent = new BookReservationEvent();
            bookReservationReverseEvent.setBookReservation(bookReservation);
            bookReservationReverseEvent.setBookReservationStatus(BookReservationStatus.REVERSED);
            kafkaTemplate.send("reversed-reservations", bookReservationEvent);
        }
    }
}
