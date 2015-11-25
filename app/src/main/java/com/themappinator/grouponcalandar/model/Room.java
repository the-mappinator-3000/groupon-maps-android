package com.themappinator.grouponcalandar.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a room in the palo alto offices
 */
public class Room {
    public String floor;
    public String readableName;
    public String googleResourceId;
    public List<TimePeriod> booked = new ArrayList<>();
    public DateTime lastUpdated;

    @Override
    public String toString() {
        return readableName + " booked:" + booked.toString();
    }
}
