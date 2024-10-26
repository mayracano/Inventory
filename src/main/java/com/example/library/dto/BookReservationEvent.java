package com.example.library.dto;

public class BookReservationEvent {
    private BookReservation bookReservation;
    private BookReservationStatus bookReservationStatus;

    public BookReservation getBookReservation() {
        return bookReservation;
    }

    public void setBookReservation(BookReservation bookReservation) {
        this.bookReservation = bookReservation;
    }

    public BookReservationStatus getBookReservationStatus() {
        return bookReservationStatus;
    }

    public void setBookReservationStatus(BookReservationStatus bookReservationStatus) {
        this.bookReservationStatus = bookReservationStatus;
    }
}
