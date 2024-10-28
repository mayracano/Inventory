package com.example.library.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.example.library.dto.BookInventoryDTO;
import com.example.library.dto.BookInventoryResponseDTO;
import com.example.library.dto.BookInventoryStatus;
import com.example.library.dto.BookReservationDTO;
import com.example.library.dto.BookReservationEvent;
import com.example.library.dto.BookReservationStatus;
import com.example.library.model.BookInventory;
import com.example.library.repository.BookInventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookInventoryServiceImpl implements BookInventoryService{

    private static final Logger LOGGER = LoggerFactory.getLogger(BookInventoryServiceImpl.class);

    @Autowired
    private BookInventoryRepository bookInventoryRepository;

    @Autowired
    KafkaTemplate<String, BookReservationEvent> kafkaTemplate;

    @KafkaListener(topics = "removed-inventory", groupId = "reservations-group")
    public void removeFromInventoryEvent(String event) throws Exception {

        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        LOGGER.info(String.format("Received 'removed-inventory', operation to remove a book from the inventory for user: %s and book: %s", bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId()));

        BookReservationDTO bookReservationDTO = bookReservationEvent.getBookReservation();
        BookInventoryDTO bookInventoryDTO = new BookInventoryDTO();
        bookInventoryDTO.setReservationID(bookReservationDTO.getId());
        bookInventoryDTO.setBookId(bookReservationDTO.getBookId());
        bookInventoryDTO.setUserId(bookReservationDTO.getUserId());

        try {
            removeFromInventory(bookInventoryDTO);
            BookReservationEvent bookReservationCompleteEvent = new BookReservationEvent();
            bookReservationCompleteEvent.setBookReservation(bookReservationDTO);
            bookReservationCompleteEvent.setBookReservationStatus(BookReservationStatus.COMPLETED);
            kafkaTemplate.send("completed-inventory", bookReservationEvent);
            LOGGER.info(String.format("Sent 'completed-inventory' for user: %s and book: %s", bookReservationCompleteEvent.getBookReservation().getBookId(), bookReservationCompleteEvent.getBookReservation().getUserId()));
        } catch(Exception e) {
            BookReservationEvent bookReservationReverseEvent = new BookReservationEvent();
            bookReservationReverseEvent.setBookReservation(bookReservationDTO);
            bookReservationReverseEvent.setBookReservationStatus(BookReservationStatus.REVERSED);
            kafkaTemplate.send("removed-inventory-failed", bookReservationEvent);
            LOGGER.info(String.format("Sent 'removed-inventory-failed' for user: %s and book: %s", bookReservationReverseEvent.getBookReservation().getBookId(), bookReservationReverseEvent.getBookReservation().getUserId()));
        }
    }

    @Override
    public BookInventoryResponseDTO addToInventory(BookInventoryDTO bookInventoryDTO) {
        BookInventory bookInventory = getExistingInventoryOrDefault(bookInventoryDTO.getBookId());

        BookInventoryResponseDTO bookInventoryResponseDTO = new BookInventoryResponseDTO();
        int newQuantity = bookInventory.getQuantity() + 1;
        bookInventory.setQuantity(newQuantity);

        bookInventoryRepository.save(bookInventory);

        bookInventoryResponseDTO.setStatus(BookInventoryStatus.AVAILABLE);
        bookInventoryResponseDTO.setBookId(bookInventoryDTO.getBookId());
        bookInventoryResponseDTO.setUserId(bookInventoryDTO.getUserId());
        bookInventoryResponseDTO.setReservationID(bookInventoryDTO.getReservationID());

        return bookInventoryResponseDTO;
    }

    @Override
    public BookInventoryResponseDTO removeFromInventory(BookInventoryDTO bookInventoryDTO) {
        BookInventory bookInventory = getExistingInventoryOrDefault(bookInventoryDTO.getBookId());

        BookInventoryResponseDTO bookInventoryResponseDTO = new BookInventoryResponseDTO();
        if (bookInventory.getQuantity() > 0) {
            int newQuantity = bookInventory.getQuantity() - 1;
            bookInventory.setQuantity(newQuantity);
        } else {
            throw new NoSuchElementException("There is no inventory for that book");
        }
        bookInventoryRepository.save(bookInventory);

        bookInventoryResponseDTO.setStatus(BookInventoryStatus.UNAVAILABLE);
        if (bookInventory.getQuantity() > 0) {
            bookInventoryResponseDTO.setStatus(BookInventoryStatus.AVAILABLE);
        }
        bookInventoryResponseDTO.setBookId(bookInventoryDTO.getBookId());
        bookInventoryResponseDTO.setUserId(bookInventoryDTO.getUserId());
        bookInventoryResponseDTO.setReservationID(bookInventoryDTO.getReservationID());

        return bookInventoryResponseDTO;
    }

    private BookInventory getExistingInventoryOrDefault(Long bookId) {
        Optional<BookInventory> optionalBookInventory = bookInventoryRepository.findByBookId(bookId);
        BookInventory bookInventory;
        if (optionalBookInventory.isEmpty()) {
            bookInventory = new BookInventory();
            bookInventory.setBookId(bookId);
            bookInventory.setQuantity(0);
            return bookInventory;
        }
        return optionalBookInventory.get();
    }
}
