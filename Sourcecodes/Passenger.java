package com.travelease.model;

public class Passenger {
    private String name;
    private int age;
    private String gender;
    private String seatNumber;

    public Passenger() {
    }

    public Passenger(String name, int age, String gender, String seatNumber) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.seatNumber = seatNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                '}';
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("          TravelEase: Passenger Model Test        ");
        System.out.println("==================================================");
        Passenger sample = new Passenger("Malavika", 21, "Female", "2A");
        System.out.println("Created Passenger instance: " + sample);
        System.out.println("[SUCCESS] Passenger model is ready!");
        System.out.println("==================================================");
    }
}
