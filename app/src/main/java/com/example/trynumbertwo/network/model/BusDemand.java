package com.example.trynumbertwo.network.model;

public class BusDemand {

    private String departure;
    private String destination;

    public BusDemand() { }

    public BusDemand(String departure, String destination) {
        this.departure = departure;
        this.destination = destination;
    }

    public String getDeparture() {
        return departure;
    }

    public String getDestination() {
        return destination;
    }

}
