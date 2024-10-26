package com.example.library.repository;

import java.util.Optional;

import com.example.library.model.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
    Optional<BookInventory> findByBookId(Long bookId);
}
