package com.themappinator.grouponcalandar.network;

import android.content.Context;
import android.content.Intent;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

/**
 * Client class used to manage api calls to google
 */
public class GoogleCalendarApiClient {
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    public GoogleCalendarApiClient(Context context, String selectedAccount) {
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

    public Calendar getCalendarService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();
    }
}
