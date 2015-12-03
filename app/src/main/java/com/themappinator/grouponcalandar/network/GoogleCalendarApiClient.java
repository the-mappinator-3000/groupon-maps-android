package com.themappinator.grouponcalandar.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.themappinator.grouponcalandar.model.Room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Client class used to manage api calls to google
 */
public class GoogleCalendarApiClient {
    public static final long ONE_DAY_MILLIES = 86400000;
    public static final long HALF_HOUR_MILLIES = 1800000;
    private static final int REQUEST_SIZE = 5;
    GoogleAccountCredential mCredential;
    Context context;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    public GoogleCalendarApiClient(Context context, String selectedAccount) {
        this.context = context;
        mCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(selectedAccount);
    }

    public void setSelectedAccountName(String selectedAccount) {
        mCredential.setSelectedAccountName(selectedAccount);
    }

    public boolean isAccountSelected() {
        return !(mCredential.getSelectedAccountName() == null);
    }

    public Intent newChooseAccountIntent() {
        return mCredential.newChooseAccountIntent();
    }

    private Calendar getCalendarService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Groupon Maps")
                .build();
    }

    /**
     * Retrives the freebusy data from google and updates the rooms with the new time periods
     * @param rooms the rooms to retrieve info for
     * @return the updated rooms
     * @throws IOException
     */
    public List<Room> getFreeBusy(List<Room> rooms) throws IOException {
        for(int i = 0; i < rooms.size(); ) {
            // List the next free booked for the given location booked from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<FreeBusyRequestItem> fbItems = new ArrayList<>();
            for (int j = i; (j < i + REQUEST_SIZE ) && (j < rooms.size()); j++) {
                fbItems.add(new FreeBusyRequestItem().setId(rooms.get(j).googleResourceId));
            }

            FreeBusyResponse freebusy = getCalendarService().freebusy().query(
                    new FreeBusyRequest()
                            .setTimeMin(now)
                            .setTimeMax(new DateTime(System.currentTimeMillis() + ONE_DAY_MILLIES))
                            .setItems(fbItems)
                    )
                    .execute();

            Map<String, FreeBusyCalendar> calendars = freebusy.getCalendars();
            Log.d("GAPICLIENT","retrieved:" + calendars.toString());
            // this is ineffecient - should fix
            for (Room room : rooms) {
                FreeBusyCalendar calendar = calendars.get(room.googleResourceId);
                if (calendar != null) {
                    room.booked = calendar.getBusy();
                }
            }
            i += REQUEST_SIZE;
        }
        return rooms;
    }

    // creates an event for half an hour starting now
    public void createEventAt(Room room) throws IOException {
        Event event = new Event();
        event.setAttendees(Arrays.asList(
                new EventAttendee().setEmail(room.googleResourceId),
                new EventAttendee().setEmail(mCredential.getSelectedAccount().name)
                ))
            .setStart(new EventDateTime().setDateTime(new DateTime(System.currentTimeMillis())))
            .setEnd(new EventDateTime().setDateTime(new DateTime(System.currentTimeMillis() + HALF_HOUR_MILLIES)));
        getCalendarService().events().insert(mCredential.getSelectedAccount().name,
                event);
    }
}
