package com.themappinator.grouponcalandar.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a room in the palo alto offices
 */
public class Room implements Parcelable{
    public static final String TAG = "room";
    public String floor;
    public String id;
    public String name;
    public String googleResourceId;
    // initially booked for all eternity
    public List<TimePeriod> booked = Arrays.asList(new TimePeriod()
                                                        .setStart(new DateTime(0))
                                                        .setEnd(new DateTime(Long.MAX_VALUE)));
    public DateTime lastUpdated;

    /**
     * Checks if the room is busy in the half hour after time
     * @param time the start of the free/busy period
     * @return true if the room is booked
     */
    public boolean isBusy(DateTime time) {
        long nowStart = time.getValue();
        long nowEnd = nowStart + GoogleCalendarApiClient.ONE_HOUR_MILLIES;
        for (TimePeriod period : booked) {
            long start = period.getStart().getValue();
            long end = period.getEnd().getValue();
            if ((start < nowStart && nowStart < end) || // booking overlaps start of hour
                    (start < nowEnd || end < nowEnd) || // booking overlaps end of hour
                    (nowStart < start && end < nowEnd)) { // booking within the hour
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name + " floor:" + floor + " booked:" + booked.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.floor);
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.googleResourceId);
        // store the number of start, end longs we are saving
        dest.writeInt(booked.size());
        for (TimePeriod t : booked ) {
            long start = t.getStart().getValue();
            long end = t.getEnd().getValue();
            dest.writeLong(start);
            dest.writeLong(end);
        }
        dest.writeSerializable(this.lastUpdated);
    }

    public Room() {
    }

    protected Room(Parcel in) {
        this.floor = in.readString();
        this.id = in.readString();
        this.name = in.readString();
        this.googleResourceId = in.readString();
        // put back all the TimePeriods into the booked list
        int count = in.readInt();
        this.booked = new ArrayList<TimePeriod>();
        for (int i = 0; i < count; i++) {
            long start = in.readLong();
            long end = in.readLong();
            booked.add(new TimePeriod().setStart(new DateTime(start)).setEnd(new DateTime(end)));
        }
        this.lastUpdated = (DateTime) in.readSerializable();
    }

    public static final Parcelable.Creator<Room> CREATOR = new Parcelable.Creator<Room>() {
        public Room createFromParcel(Parcel source) {
            return new Room(source);
        }

        public Room[] newArray(int size) {
            return new Room[size];
        }
    };
}
