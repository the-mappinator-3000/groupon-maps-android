package com.themappinator.grouponcalandar.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;
import com.themappinator.grouponcalandar.ui.MapImageView;

public class MapActivity extends AppCompatActivity {

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private Room selectedRoom;
    ProgressDialog mProgress;

    private MapImageView mapImageView;
    private GoogleCalendarApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        selectedRoom = intent.getParcelableExtra("room");

        client = GoogleCalendarApiClient.getInstance();

        mapImageView = (MapImageView)findViewById(R.id.mapImageView);
        mapImageView.selectRoom(selectedRoom);
        mProgress = new ProgressDialog(this);
        mapImageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MakeRequestTask(client).execute(selectedRoom);
                    }
                }
        );
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Room, Void, Void> {
        GoogleCalendarApiClient client;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleCalendarApiClient client) {
            this.client = client;
        }

        /**
         * Background task to call Google Calendar API.
         */
        @Override
        protected Void doInBackground(Room... params) {
            try {
                client.createEventAt(params[0]);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgress.hide();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                Log.e("Create failure", mLastError.getMessage());
            }
        }
    }
}
