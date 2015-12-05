package com.themappinator.grouponcalandar.utils;

import android.app.Activity;
import android.app.Dialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;

/**
 * Created by ayegorov on 12/4/15.
 */
public class GooglePlayServicesManager {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private static GooglePlayServicesManager instance;

    private Activity activity;
    private GoogleCalendarApiClient client;

    public static GooglePlayServicesManager instance() {
        if (instance == null) {
            instance = new GooglePlayServicesManager();
        }

        return instance;
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    public boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                activity,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    public void chooseAccount() {
        activity.startActivityForResult(client.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    public void handleCancelException(Exception exception) {
        if (exception != null) {
            if (exception instanceof GooglePlayServicesAvailabilityIOException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) exception)
                                .getConnectionStatusCode());
            } else if (exception instanceof UserRecoverableAuthIOException) {
                activity.startActivityForResult(
                        ((UserRecoverableAuthIOException) exception).getIntent(),
                        REQUEST_AUTHORIZATION);
            } else {
                //mOutputText.setText("The following error occurred:\n"
                //        + mLastError.getMessage());
            }
        } else {
            //mOutputText.setText("Request cancelled.");
        }
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setClient(GoogleCalendarApiClient client) {
        this.client = client;
    }
}
