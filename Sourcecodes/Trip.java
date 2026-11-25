
package com.travelease.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Trip {
    private int tripId;
    private String transportType; // 'BUS' or 'TRAIN'
    private String serviceName;
    private String source;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private BigDecimal fare;
    private int totalSeats;
    private int availableSeats; // Dynamically calculated
    private String status; // 'ACTIVE' or 'INACTIVE'
    private Timestamp createdAt;

    public Trip() {
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public BigDecimal getFare() {
        return fare;
    }

    public void setFare(BigDecimal fare) {
        this.fare = fare;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId=" + tripId +
                ", transportType='" + transportType + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", fare=" + fare +
                ", availableSeats=" + availableSeats +
                '}';
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("            TravelEase: Trip Model Test           ");
        System.out.println("==================================================");
        Trip sample = new Trip();
        sample.setTripId(1);
        sample.setTransportType("BUS");
        sample.setServiceName("GreenLine Express");
        sample.setSource("Bangalore");
        sample.setDestination("Chennai");
        sample.setDepartureTime("06:00:00");
        sample.setArrivalTime("12:00:00");
        sample.setFare(new BigDecimal("750.00"));
        sample.setTotalSeats(32);
        sample.setAvailableSeats(28);
        sample.setStatus("ACTIVE");
        System.out.println("Created Trip instance: " + sample);
        System.out.println("[SUCCESS] Trip model is ready!");
        System.out.println("==================================================");
    }
}
