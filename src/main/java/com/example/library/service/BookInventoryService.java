package com.example.library.service;

import com.example.library.dto.BookInventoryDTO;
import com.example.library.dto.BookInventoryResponseDTO;

public interface BookInventoryService {

    void removeFromInventoryEvent(String event) throws Exception;

    BookInventoryResponseDTO addToInventory(BookInventoryDTO bookInventoryDTO);

    BookInventoryResponseDTO removeFromInventory(BookInventoryDTO bookInventoryDTO);

}
