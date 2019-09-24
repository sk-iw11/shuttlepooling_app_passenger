package com.example.trynumbertwo.network.model;

import java.util.ArrayList;
import java.util.List;

public class AssignedBus {

    private String name;

    private List<String> route;

    public AssignedBus() { }

    public AssignedBus(String name, List<String> route) {
        this.name = name;
        this.route = new ArrayList<>(route);
    }

    public String getName() {
        return name;
    }

    public List<String> getRoute() {
        return new ArrayList<>(route);
    }
}
