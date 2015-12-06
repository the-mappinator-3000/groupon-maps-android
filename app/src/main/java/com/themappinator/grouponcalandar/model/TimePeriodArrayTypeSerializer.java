package com.themappinator.grouponcalandar.model;

import android.util.Log;

import com.activeandroid.serializer.TypeSerializer;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;

import java.util.ArrayList;

/**
 * Used by activeandroid to serialize and deserialize the TimePeriod[]
 */
public class TimePeriodArrayTypeSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return TimePeriod[].class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public Object serialize(Object data) {
        if (data == null) {
            return null;
        }
        TimePeriod[] array = (TimePeriod[]) data;
        StringBuilder builder = new StringBuilder();
        for (TimePeriod t : array) {
            long start = t.getStart().getValue();
            long end = t.getEnd().getValue();
            builder.append(start).append(":").append(end);
            builder.append(",");
        }
        return builder.toString();
    }

    @Override
    public Object deserialize(Object data) {
        if (data == null) {
            return null;
        }
        ArrayList<TimePeriod> bookedList = new ArrayList<>();
        String dataString = (String) data;
        String[] bookings = dataString.split(",");
        for (String booking : bookings) {
            if (!booking.isEmpty()) {
                String[] startAndEnd = booking.split(":");
                if (startAndEnd.length != 2) {
                    Log.e("TPATS", "error deserializing a booking");
                    continue;
                }
                long start = Long.parseLong(startAndEnd[0]);
                long end = Long.parseLong(startAndEnd[1]);
                bookedList.add(new TimePeriod().setStart(new DateTime(start))
                                                .setEnd(new DateTime(end)));
            }
        }
        return bookedList.toArray(new TimePeriod[bookedList.size()]);
    }
}
