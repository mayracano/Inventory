package com.example.library.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.example.library.dto.BookInventoryDTO;
import com.example.library.dto.BookInventoryResponseDTO;
import com.example.library.dto.BookInventoryStatus;
import com.example.library.model.BookInventory;
import com.example.library.repository.BookInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookInventoryServiceImpl implements BookInventoryService {

    @Autowired
    private BookInventoryRepository bookInventoryRepository;

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
