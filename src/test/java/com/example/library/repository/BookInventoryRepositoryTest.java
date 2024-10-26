package com.example.library.repository;

import java.util.List;

import com.example.library.model.BookInventory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
public class BookInventoryRepositoryTest {

    @Autowired
    private BookInventoryRepository bookInventoryRepository;

    @Test
    public void saveInventoryBook() {
        BookInventory bookInventory = new BookInventory();
        bookInventory.setBookId(2l);
        bookInventory.setQuantity(1);
        bookInventoryRepository.save(bookInventory);
        Assertions.assertThat(bookInventory.getInventoryId()).isGreaterThan(0);
    }

    @Test
    public void saveInventoryUniqueConstraintBook() {
        BookInventory bookInventory = new BookInventory();
        bookInventory.setBookId(1l);
        bookInventory.setQuantity(1);
        assertThrows(DataIntegrityViolationException.class, ()-> bookInventoryRepository.save(bookInventory));
    }

    @Test
    public void updateInventoryBook() {
        BookInventory bookInventory = bookInventoryRepository.findById(1L).get();
        bookInventory.setQuantity(3);
        bookInventoryRepository.save(bookInventory);
        Assertions.assertThat(bookInventory.getInventoryId()).isEqualTo(1l);
        Assertions.assertThat(bookInventory.getBookId()).isEqualTo(1l);
        Assertions.assertThat(bookInventory.getQuantity()).isEqualTo(3);
    }

    @Test
    public void getBookTest(){
        BookInventory bookInventory = bookInventoryRepository.findById(1L).get();
        assertThat(bookInventory.getInventoryId()).isEqualTo(1L);
    }

    @Test
    public void getListOfBooksTest(){
        List<BookInventory> books = bookInventoryRepository.findAll();
        Assertions.assertThat(books.size()).isGreaterThan(0);
    }
}
