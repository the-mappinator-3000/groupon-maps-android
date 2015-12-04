package com.themappinator.grouponcalandar.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BookEventActivity extends AppCompatActivity {

    private Room selectedRoom;
    private GoogleCalendarApiClient client;
    private ProgressDialog mProgress;
    @Bind(R.id.tvSummary) TextView tvSummary;
    @Bind(R.id.etSummary) EditText etSummary;
    private String summaryText;
    @Bind(R.id.etDetails) EditText etDetails;
    private String detailsText;
    @Bind(R.id.btnBook) Button btnBook;
    @Bind(R.id.btnCancel) Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_event);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        selectedRoom = intent.getParcelableExtra(Room.TAG);
        client = GoogleCalendarApiClient.getInstance();
        mProgress = new ProgressDialog(this);

        tvSummary.setText(getResources().getString(R.string.room_booking_title, selectedRoom.name));
        setupButtons();
        setupEditText();
    }

    private void setupEditText() {
        etSummary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                summaryText = s.toString();
            }
        });

        etDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                detailsText = s.toString();
            }
        });
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MakeRequestTask(client).execute(selectedRoom);
            }
        });
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
                long now = System.currentTimeMillis();
                client.createEventAt(params[0],
                        new DateTime(now),
                        new DateTime(now + GoogleCalendarApiClient.THREE_QUATER_HOUR_MILLIES),
                        summaryText,
                        detailsText);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgress.hide();
            Intent i = new Intent(BookEventActivity.this, MapActivity.class);
            i.putExtra(Room.TAG, selectedRoom);
            startActivity(i);
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
