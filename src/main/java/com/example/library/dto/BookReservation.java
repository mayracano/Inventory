package com.example.library.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookReservation {
    private Long id;
    private Long userId;
    private Long bookId;
}
