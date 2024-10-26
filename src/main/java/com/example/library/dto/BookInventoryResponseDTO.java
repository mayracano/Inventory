package com.example.library.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookInventoryResponseDTO {
    private Long userId;
    private Long bookId;
    private Long reservationID;
    private BookInventoryStatus status;
}
