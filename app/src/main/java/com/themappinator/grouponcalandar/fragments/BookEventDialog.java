package com.themappinator.grouponcalandar.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.activities.MapActivity;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lsilberstein on 12/6/15.
 */
public class BookEventDialog extends DialogFragment {
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

    public BookEventDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static BookEventDialog newInstance(Room room) {
        BookEventDialog frag = new BookEventDialog();
        Bundle args = new Bundle();
        args.putParcelable(Room.TAG, room);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_event, container);
        ButterKnife.bind(this, view);
        Bundle args = getArguments();
        selectedRoom = args.getParcelable(Room.TAG);
        client = GoogleCalendarApiClient.getInstance();
        mProgress = new ProgressDialog(getContext());

        tvSummary.setText(getResources().getString(R.string.room_booking_title, selectedRoom.name));
        setupButtons();
        setupEditText();
        return view;
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
                dismiss();
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
            Intent i = new Intent(getContext(), MapActivity.class);
            i.putExtra(Room.TAG, selectedRoom);
            dismiss();
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
