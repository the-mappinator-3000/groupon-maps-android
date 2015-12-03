package com.themappinator.grouponcalandar.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a room in the palo alto offices
 */
public class Room implements Parcelable {
    public String floor;
    public String id;
    public String name;
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
        // TODO: Figure out why I have a crash caused by:
        // TODO: java.lang.RuntimeException: Parcelable encountered ClassNotFoundException reading a Serializable object (name = com.google.api.client.util.DateTime)
        // TODO:                         (AvY)
//        dest.writeList(this.booked);
        dest.writeSerializable(this.lastUpdated);
    }

    public Room() {
    }

    protected Room(Parcel in) {
        this.floor = in.readString();
        this.id = in.readString();
        this.name = in.readString();
        this.googleResourceId = in.readString();
        this.booked = new ArrayList<TimePeriod>();
        in.readList(this.booked, List.class.getClassLoader());
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
