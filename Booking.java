package com.travelease.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Booking {
    private int bookingId;
    private String bookingReference;
    private int userId;
    private int tripId;
    private Date journeyDate;
    private BigDecimal totalAmount;
    private String status; // 'CONFIRMED', 'CANCELLED', 'COMPLETED'
    private Timestamp bookingDate;

    // Supplementary Journey Details (for display on dashboard & confirmation)
    private String serviceName;
    private String transportType;
    private String source;
    private String destination;
    private String departureTime;
    private String arrivalTime;

    private List<String> seats = new ArrayList<>();
    private List<Passenger> passengers = new ArrayList<>();

    public Booking() {
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public Date getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(Date journeyDate) {
        this.journeyDate = journeyDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Timestamp bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", bookingReference='" + bookingReference + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", journeyDate=" + journeyDate +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", seats=" + seats +
                '}';
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("          TravelEase: Booking Model Test          ");
        System.out.println("==================================================");
        Booking sample = new Booking();
        sample.setBookingId(101);
        sample.setBookingReference("BK-20261026-TEST");
        sample.setServiceName("GreenLine Express");
        sample.setSource("Bangalore");
        sample.setDestination("Chennai");
        sample.setTotalAmount(new BigDecimal("750.00"));
        sample.setStatus("CONFIRMED");
        sample.getSeats().add("1A");
        System.out.println("Created Booking instance: " + sample);
        System.out.println("[SUCCESS] Booking model is ready!");
        System.out.println("==================================================");
    }
}
