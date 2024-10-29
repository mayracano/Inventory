package com.example.library.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookReservationEvent {
    private BookReservationDTO bookReservation;
    private BookReservationStatus bookReservationStatus;
    private BookInventoryStatus bookInventoryStatus;
}
