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
    public String id;
    public String googleResourceId;
    public List<TimePeriod> booked = new ArrayList<>();
    public DateTime lastUpdated;

    public boolean isBusy(DateTime time) {
        for (TimePeriod period : booked) {
            if(period.getStart().getValue() < time.getValue() && time.getValue() < period.getEnd().getValue()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return id + " booked:" + booked.toString();
    }
}
