package com.example.library.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.example.library.dto.BookInventoryDTO;
import com.example.library.dto.BookInventoryResponseDTO;
import com.example.library.dto.BookInventoryStatus;
import com.example.library.model.BookInventory;
import com.example.library.repository.BookInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class BookInventoryServiceTest {

    @InjectMocks
    private BookInventoryServiceImpl bookInventoryService;

    @Mock
    private BookInventoryRepository inventoryRepository;

    private BookInventoryDTO bookInventoryDTO;

    private BookInventoryResponseDTO bookInventoryResponseDTO;

    @BeforeEach
    public void setup() {
        bookInventoryDTO = new BookInventoryDTO();
        bookInventoryDTO.setBookId(1l);
        bookInventoryDTO.setReservationID(1L);
        bookInventoryDTO.setUserId(1l);

        bookInventoryResponseDTO = new BookInventoryResponseDTO();
        bookInventoryResponseDTO.setBookId(1l);
        bookInventoryResponseDTO.setReservationID(1L);
        bookInventoryResponseDTO.setUserId(1l);
        bookInventoryResponseDTO.setStatus(BookInventoryStatus.AVAILABLE);
    }

    @Test
    public void addToInventoryTest() {
        BookInventory bookInventory = new BookInventory();
        bookInventory.setInventoryId(1l);
        given(inventoryRepository.findByBookId(1l)).willReturn(Optional.of(bookInventory));
        BookInventoryResponseDTO response = bookInventoryService.addToInventory(bookInventoryDTO);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookInventoryStatus.AVAILABLE);
    }

    @Test
    public void addToNonExistentInventoryTest() {
        given(inventoryRepository.findByBookId(1l)).willReturn(Optional.empty());
        BookInventoryResponseDTO response = bookInventoryService.addToInventory(bookInventoryDTO);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookInventoryStatus.AVAILABLE);
    }

    @Test
    public void removeFromInventoryTest() {
        BookInventory bookInventory = new BookInventory();
        bookInventory.setInventoryId(1l);
        bookInventory.setQuantity(2);
        given(inventoryRepository.findByBookId(1l)).willReturn(Optional.of(bookInventory));
        BookInventoryResponseDTO response = bookInventoryService.removeFromInventory(bookInventoryDTO);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookInventoryStatus.AVAILABLE);
    }

    @Test
    public void removeFromInventoryLastElementTest() {
        BookInventory bookInventory = new BookInventory();
        bookInventory.setInventoryId(1l);
        bookInventory.setQuantity(1);
        given(inventoryRepository.findByBookId(1l)).willReturn(Optional.of(bookInventory));
        BookInventoryResponseDTO response = bookInventoryService.removeFromInventory(bookInventoryDTO);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookInventoryStatus.UNAVAILABLE);
    }

    @Test
    public void removeFromNonExistInventoryTest() {
        given(inventoryRepository.findByBookId(1l)).willReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, ()-> bookInventoryService.removeFromInventory(bookInventoryDTO));
    }
}
