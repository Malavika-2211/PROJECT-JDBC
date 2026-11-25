package com.travelease.model;

public class Seat {
    private int seatId;
    private int tripId;
    private String seatNumber;
    private int rowNum;
    private String colLetter;
    private String status; // 'AVAILABLE' or 'BOOKED' (Calculated dynamically per journey date)

    public Seat() {
    }

    public Seat(int seatId, int tripId, String seatNumber, int rowNum, String colLetter, String status) {
        this.seatId = seatId;
        this.tripId = tripId;
        this.seatNumber = seatNumber;
        this.rowNum = rowNum;
        this.colLetter = colLetter;
        this.status = status;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getColLetter() {
        return colLetter;
    }

    public void setColLetter(String colLetter) {
        this.colLetter = colLetter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", tripId=" + tripId +
                ", seatNumber='" + seatNumber + '\'' +
                ", rowNum=" + rowNum +
                ", colLetter='" + colLetter + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("            TravelEase: Seat Model Test           ");
        System.out.println("==================================================");
        Seat sample = new Seat(1, 1, "1A", 1, "A", "AVAILABLE");
        System.out.println("Created Seat instance: " + sample);
        System.out.println("[SUCCESS] Seat model is ready!");
        System.out.println("==================================================");
    }
}
