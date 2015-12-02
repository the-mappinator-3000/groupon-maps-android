package com.themappinator.grouponcalandar.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;

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

    /**
     * Checks if the room is busy in the half hour after time
     * @param time the start of the free/busy period
     * @return true if the room is booked
     */
    public boolean isBusy(DateTime time) {
        for (TimePeriod period : booked) {
            if(period.getStart().getValue() < time.getValue() && time.getValue() + GoogleCalendarApiClient.HALF_HOUR_MILLIES < period.getEnd().getValue()) {
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
